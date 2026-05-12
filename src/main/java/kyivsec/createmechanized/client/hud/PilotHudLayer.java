package kyivsec.createmechanized.client.hud;

import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.companion.ClientSubLevelAccess;
import dev.ryanhcode.sable.companion.SubLevelAccess;
import kyivsec.createmechanized.CreateMechanizedMod;
import kyivsec.createmechanized.content.pilot_helmet.PilotHelmetWearableItem;
import kyivsec.createmechanized.content.pilot_helmet.TrackedContainer;
import kyivsec.createmechanized.content.pilot_helmet.TrackedContainerList;
import kyivsec.createmechanized.registry.ModDataComponents;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import java.util.List;

public final class PilotHudLayer implements LayeredDraw.Layer {

    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(CreateMechanizedMod.MODID, "pilot_hud");

    @Override
    public void render(GuiGraphics graphics, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui) {
            return;
        }
        LocalPlayer player = mc.player;
        if (player == null) {
            return;
        }
        if (!PilotHelmetWearableItem.isWornBy(player)) {
            return;
        }

        float partialTick = delta.getGameTimeDeltaPartialTick(true);
        ClientSubLevelAccess sub = tryResolveSubLevel(player);

        FlightData data;
        if (sub != null) {
            data = FlightDataSource.fromSubLevel(sub, partialTick);
        } else if (PilotHelmetWearableItem.hasElytraEquipped(player)) {
            data = FlightDataSource.fromPlayerFlying(player, partialTick);
        } else {
            data = FlightDataSource.fromPlayer(player);
        }

        ItemStack helmetStack = player.getItemBySlot(EquipmentSlot.HEAD);
        int colorRgb = PilotHelmetWearableItem.getColorRgb(helmetStack);

        HudRenderer.Palette palette = HudRenderer.Palette.fromRgb(colorRgb);
        HudRenderer.draw(graphics, data, colorRgb);

        drawContainerList(graphics, mc, helmetStack, palette);
    }

    private static void drawContainerList(GuiGraphics graphics, Minecraft mc, ItemStack helmetStack, HudRenderer.Palette palette) {
        TrackedContainerList list = helmetStack.get(ModDataComponents.TRACKED_CONTAINERS.get());
        if (list == null) return;
        List<TrackedContainer> entries = list.entries();
        if (entries.isEmpty()) return;

        Font font = mc.font;
        int rowH = 16;
        int barH = 4;
        int barW = 90;
        int colWidth = 100;
        int rowsPerCol = 9;
        int xLeft = 8;
        int leftColumnRows = Math.min(entries.size(), rowsPerCol);
        int yBase = graphics.guiHeight() - 8 - leftColumnRows * rowH;

        for (int i = 0; i < entries.size(); i++) {
            TrackedContainer c = entries.get(i);
            float fraction = resolveFillFraction(c, mc.level);
            int col = i / rowsPerCol;
            int row = i % rowsPerCol;
            int x = xLeft + col * colWidth;
            int y = yBase + row * rowH;
            graphics.drawString(font, c.name(), x, y, 0xFFFFFFFF, false);
            HudRenderer.drawProgressBar(graphics, x, y + 9, barW, barH, fraction, palette);
        }
    }

    private static float resolveFillFraction(TrackedContainer c, Level level) {
        if (level == null) return 0f;
        try {
            if (c.kind() == TrackedContainer.Kind.FLUID) {
                IFluidHandler h = level.getCapability(Capabilities.FluidHandler.BLOCK, c.pos(), null);
                if (h == null) return 0f;
                long sum = 0;
                long cap = 0;
                int tanks = h.getTanks();
                for (int i = 0; i < tanks; i++) {
                    sum += h.getFluidInTank(i).getAmount();
                    cap += h.getTankCapacity(i);
                }
                return cap == 0 ? 0f : (float) ((double) sum / (double) cap);
            } else {
                IEnergyStorage h = level.getCapability(Capabilities.EnergyStorage.BLOCK, c.pos(), null);
                if (h == null) return 0f;
                int cap = h.getMaxEnergyStored();
                return cap == 0 ? 0f : (float) h.getEnergyStored() / cap;
            }
        } catch (Throwable t) {
            return 0f;
        }
    }

    private static ClientSubLevelAccess tryResolveSubLevel(LocalPlayer player) {
        try {
            SubLevelAccess access = Sable.HELPER.getTrackingOrVehicleSubLevel(player);
            if (access == null) {
                access = Sable.HELPER.getContaining(player);
            }
            if (access instanceof ClientSubLevelAccess client) {
                return client;
            }
        } catch (Throwable t) {
            CreateMechanizedMod.LOGGER.debug("Pilot HUD: sublevel resolution failed", t);
        }
        return null;
    }
}
