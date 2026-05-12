package kyivsec.createmechanized.network;

import kyivsec.createmechanized.CreateMechanizedMod;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = CreateMechanizedMod.MODID)
public final class ModNetwork {

    private ModNetwork() {
    }

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(CreateMechanizedMod.MODID).versioned("1");
        registrar.playToServer(
                RegisterContainerPacket.TYPE, RegisterContainerPacket.STREAM_CODEC,
                RegisterContainerPacket::handle
        );
        registrar.playToServer(
                ResetTrackingPacket.TYPE, ResetTrackingPacket.STREAM_CODEC,
                ResetTrackingPacket::handle
        );
    }
}
