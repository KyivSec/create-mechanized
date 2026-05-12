package kyivsec.createmechanized.content.pilot_helmet.client;

import kyivsec.createmechanized.CreateMechanizedMod;
import kyivsec.createmechanized.content.pilot_helmet.PilotHelmetWearableItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;

@EventBusSubscriber(modid = CreateMechanizedMod.MODID, value = Dist.CLIENT)
public final class PilotHelmetEvents {

    private PilotHelmetEvents() {
    }

    @SubscribeEvent
    public static void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        Player player = event.getEntity();
        if (PilotHelmetWearableItem.isWornBy(player)) {
            event.getRenderer().getModel().hat.visible = false;
        }
    }

    @SubscribeEvent
    public static void onRenderPlayerPost(RenderPlayerEvent.Post event) {
        Player player = event.getEntity();
        event.getRenderer().getModel().hat.visible = player.isModelPartShown(PlayerModelPart.HAT);
    }
}
