package kyivsec.createmechanized.client.hud;

import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.companion.ClientSubLevelAccess;
import dev.ryanhcode.sable.companion.SubLevelAccess;
import kyivsec.createmechanized.CreateMechanizedMod;
import kyivsec.createmechanized.content.pilot_helmet.PilotHelmetWearableItem;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

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

        HudRenderer.draw(graphics, data, colorRgb);
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
