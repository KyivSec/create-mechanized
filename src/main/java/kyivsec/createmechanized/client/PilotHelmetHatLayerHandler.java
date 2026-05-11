package kyivsec.createmechanized.client;

import kyivsec.createmechanized.CreateMechanizedMod;
import kyivsec.createmechanized.ModItems;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;

@EventBusSubscriber(modid = CreateMechanizedMod.MODID, value = Dist.CLIENT)
public final class PilotHelmetHatLayerHandler {
    private PilotHelmetHatLayerHandler() {
    }

    @SubscribeEvent
    public static void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        Player player = event.getEntity();
        boolean wearingPilotHelmet = player.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.PILOT_HELMET.get());

        if (wearingPilotHelmet) {
            event.getRenderer().getModel().hat.visible = false;
        }
    }

    @SubscribeEvent
    public static void onRenderPlayerPost(RenderPlayerEvent.Post event) {
        Player player = event.getEntity();
        event.getRenderer().getModel().hat.visible = player.isModelPartShown(net.minecraft.world.entity.player.PlayerModelPart.HAT);
    }
}
