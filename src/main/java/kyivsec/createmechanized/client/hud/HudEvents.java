package kyivsec.createmechanized.client.hud;

import kyivsec.createmechanized.CreateMechanizedMod;
import kyivsec.createmechanized.content.pilot_helmet.PilotHelmetWearableItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(modid = CreateMechanizedMod.MODID, value = Dist.CLIENT)
public final class HudEvents {

    private HudEvents() {
    }

    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.CROSSHAIR, PilotHudLayer.ID, new PilotHudLayer());
    }

    @SubscribeEvent
    public static void onRenderGuiLayerPre(RenderGuiLayerEvent.Pre event) {
        if (!event.getName().equals(VanillaGuiLayers.CROSSHAIR)) {
            return;
        }
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null && PilotHelmetWearableItem.isWornBy(player)) {
            event.setCanceled(true);
        }
    }
}
