package kyivsec.createmechanized;

import com.mojang.logging.LogUtils;
import kyivsec.createmechanized.registry.ModCreativeTabs;
import kyivsec.createmechanized.registry.ModDataComponents;
import kyivsec.createmechanized.registry.ModItems;
import kyivsec.createmechanized.registry.ModRecipeSerializers;
import kyivsec.createmechanized.registry.ModSoundEvents;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

@Mod(CreateMechanizedMod.MODID)
public class CreateMechanizedMod {
    public static final String MODID = "createmechanized";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CreateMechanizedMod(IEventBus modEventBus, ModContainer modContainer) {
        ModItems.register(modEventBus);
        ModDataComponents.register(modEventBus);
        ModRecipeSerializers.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
        ModSoundEvents.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.COMMON, CreateMechanizedConfig.COMMON_SPEC);
    }
}
