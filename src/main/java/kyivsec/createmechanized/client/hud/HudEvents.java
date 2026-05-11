package kyivsec.createmechanized.client.hud;

import kyivsec.createmechanized.CreateMechanizedMod;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

/**
 * Registers {@link PilotHudLayer} on the in-game GUI layer stack.
 *
 * <p>{@link RegisterGuiLayersEvent} is a mod-bus startup event; NeoForge 21.1+
 * auto-routes it based on the event class so {@code bus = MOD} is unnecessary.</p>
 */
@EventBusSubscriber(modid = CreateMechanizedMod.MODID, value = Dist.CLIENT)
public final class HudEvents {

    private HudEvents() {
    }

    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        // Render after the crosshair / hotbar / experience bar so the helmet HUD is on top
        // of the standard GUI but still beneath chat / debug overlays.
        event.registerAbove(VanillaGuiLayers.CROSSHAIR, PilotHudLayer.ID, new PilotHudLayer());
    }
}
