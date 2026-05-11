package kyivsec.createmechanized;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CreateMechanizedMod.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = TABS.register(
            "main",
            () -> CreativeModeTab.builder()
                    .title(Component.literal("Create: Mechanized"))
                    .icon(() -> new ItemStack(ModItems.PILOT_HELMET.get()))
                    .displayItems(ModCreativeTabs::buildTabContents)
                    .build()
    );

    private ModCreativeTabs() {
    }

    private static void buildTabContents(CreativeModeTab.ItemDisplayParameters params, CreativeModeTab.Output output) {
        output.accept(ModItems.PILOT_HELMET.get());
    }

    public static void register(IEventBus modEventBus) {
        TABS.register(modEventBus);
    }
}
