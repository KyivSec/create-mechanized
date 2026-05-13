package kyivsec.createmechanized.registry;

import kyivsec.createmechanized.CreateMechanizedMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModSoundEvents {

    private static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(Registries.SOUND_EVENT, CreateMechanizedMod.MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> ALTITUDE_WARNING = SOUNDS.register(
            "warnings.altitude",
            () -> SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(CreateMechanizedMod.MODID, "warnings.altitude"))
    );

    public static final DeferredHolder<SoundEvent, SoundEvent> PULL_UP = SOUNDS.register(
            "warnings.pull_up",
            () -> SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(CreateMechanizedMod.MODID, "warnings.pull_up"))
    );

    public static final DeferredHolder<SoundEvent, SoundEvent> FUEL_LOW = SOUNDS.register(
            "warnings.fuel_low",
            () -> SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(CreateMechanizedMod.MODID, "warnings.fuel_low"))
    );

    private ModSoundEvents() {
    }

    public static void register(IEventBus modEventBus) {
        SOUNDS.register(modEventBus);
    }
}
