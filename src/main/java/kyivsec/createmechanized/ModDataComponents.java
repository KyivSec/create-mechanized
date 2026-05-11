package kyivsec.createmechanized;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * Registry of mod-defined {@link DataComponentType data components} attached to {@link net.minecraft.world.item.ItemStack}s.
 *
 * <p>Only one component so far: {@link #PILOT_HELMET_COLOR}, an {@code Integer}
 * holding a {@code 0xRRGGBB} palette entry from {@link PilotHelmetColor}. Stored
 * on the helmet stack after a successful dye-crafting recipe.</p>
 */
public final class ModDataComponents {

    public static final DeferredRegister.DataComponents COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, CreateMechanizedMod.MODID);

    /** RGB tint applied to the pilot HUD when this helmet is equipped. */
    public static final Supplier<DataComponentType<Integer>> PILOT_HELMET_COLOR =
            COMPONENTS.registerComponentType(
                    "pilot_helmet_color",
                    builder -> builder
                            .persistent(Codec.INT)
                            .networkSynchronized(ByteBufCodecs.INT)
            );

    private ModDataComponents() {
    }

    public static void register(IEventBus modEventBus) {
        COMPONENTS.register(modEventBus);
    }
}
