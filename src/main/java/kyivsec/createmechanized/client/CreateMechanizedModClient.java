package kyivsec.createmechanized.client;

import kyivsec.createmechanized.CreateMechanizedMod;
import kyivsec.createmechanized.client.hud.HudLayoutLoader;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;

@Mod(value = CreateMechanizedMod.MODID, dist = Dist.CLIENT)
public class CreateMechanizedModClient {
    public CreateMechanizedModClient(ModContainer container, IEventBus modEventBus) {
        modEventBus.addListener(this::onRegisterClientReloadListeners);
    }

    private void onRegisterClientReloadListeners(RegisterClientReloadListenersEvent event) {
        HudLayoutLoader.register(event);
    }
}
