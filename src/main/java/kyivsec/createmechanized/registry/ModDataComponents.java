package kyivsec.createmechanized.registry;

import com.mojang.serialization.Codec;
import kyivsec.createmechanized.CreateMechanizedMod;
import kyivsec.createmechanized.content.pilot_helmet.TrackedContainerList;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class ModDataComponents {

    public static final DeferredRegister.DataComponents COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, CreateMechanizedMod.MODID);

    public static final Supplier<DataComponentType<Integer>> PILOT_HELMET_COLOR =
            COMPONENTS.registerComponentType(
                    "pilot_helmet_color",
                    builder -> builder
                            .persistent(Codec.INT)
                            .networkSynchronized(ByteBufCodecs.INT)
            );

    public static final Supplier<DataComponentType<TrackedContainerList>> TRACKED_CONTAINERS =
            COMPONENTS.registerComponentType(
                    "tracked_containers",
                    builder -> builder
                            .persistent(TrackedContainerList.CODEC)
                            .networkSynchronized(TrackedContainerList.STREAM_CODEC)
            );

    private ModDataComponents() {
    }

    public static void register(IEventBus modEventBus) {
        COMPONENTS.register(modEventBus);
    }
}
