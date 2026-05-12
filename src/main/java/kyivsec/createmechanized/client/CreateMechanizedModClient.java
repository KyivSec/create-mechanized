package kyivsec.createmechanized.client;

import kyivsec.createmechanized.CreateMechanizedMod;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(value = CreateMechanizedMod.MODID, dist = Dist.CLIENT)
public class CreateMechanizedModClient {
    public CreateMechanizedModClient(ModContainer container, IEventBus modEventBus) {
    }
}
