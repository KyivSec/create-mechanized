package kyivsec.createmechanized.client.hud;

import kyivsec.createmechanized.CreateMechanizedConfig;
import kyivsec.createmechanized.content.pilot_helmet.TrackedContainer;
import kyivsec.createmechanized.content.pilot_helmet.TrackedContainerList;
import kyivsec.createmechanized.registry.ModSoundEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import javax.annotation.Nullable;

@OnlyIn(Dist.CLIENT)
public final class VoiceWarningSystem {

    private static long lastAltitudeTick = Long.MIN_VALUE;
    private static long lastPullUpTick   = Long.MIN_VALUE;
    private static long lastFuelLowTick  = Long.MIN_VALUE;

    private VoiceWarningSystem() {
    }

    public static void tick(LocalPlayer player, FlightData data, @Nullable TrackedContainerList containers) {
        if (data.minimal()) return;

        long now = player.level().getGameTime();
        long cooldown = CreateMechanizedConfig.WARNING_COOLDOWN_TICKS.get();

        double altitude = data.altitude();
        boolean pullUpFired = false;

        // Pull-up: low altitude + nose down — highest priority
        if (altitude < CreateMechanizedConfig.PULL_UP_WARN_METERS.get() && data.pitchRad() < 0) {
            if (now - lastPullUpTick >= cooldown) {
                play(ModSoundEvents.PULL_UP.value());
                lastPullUpTick = now;
                pullUpFired = true;
            }
        }

        // Altitude warning: only if pull-up didn't fire this tick
        if (!pullUpFired && altitude < CreateMechanizedConfig.ALTITUDE_WARN_METERS.get()) {
            if (now - lastAltitudeTick >= cooldown) {
                play(ModSoundEvents.ALTITUDE_WARNING.value());
                lastAltitudeTick = now;
            }
        }

        // Fuel low: any tracked container below threshold
        if (containers != null && now - lastFuelLowTick >= cooldown) {
            Level level = player.level();
            double threshold = CreateMechanizedConfig.FUEL_LOW_THRESHOLD.get();
            for (TrackedContainer c : containers.entries()) {
                float fraction = resolveFillFraction(c, level);
                if (fraction < threshold) {
                    play(ModSoundEvents.FUEL_LOW.value());
                    lastFuelLowTick = now;
                    break;
                }
            }
        }
    }

    private static void play(SoundEvent event) {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(event, 1.0f));
    }

    private static float resolveFillFraction(TrackedContainer c, Level level) {
        if (level == null) return 1f;
        try {
            if (c.kind() == TrackedContainer.Kind.FLUID) {
                IFluidHandler h = level.getCapability(Capabilities.FluidHandler.BLOCK, c.pos(), null);
                if (h == null) return 1f;
                long sum = 0, cap = 0;
                for (int i = 0; i < h.getTanks(); i++) {
                    sum += h.getFluidInTank(i).getAmount();
                    cap += h.getTankCapacity(i);
                }
                return cap == 0 ? 1f : (float) ((double) sum / cap);
            } else {
                IEnergyStorage h = level.getCapability(Capabilities.EnergyStorage.BLOCK, c.pos(), null);
                if (h == null) return 1f;
                int cap = h.getMaxEnergyStored();
                return cap == 0 ? 1f : (float) h.getEnergyStored() / cap;
            }
        } catch (Throwable t) {
            return 1f;
        }
    }
}
