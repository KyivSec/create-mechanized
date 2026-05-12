package kyivsec.createmechanized.registry;

import kyivsec.createmechanized.CreateMechanizedMod;
import kyivsec.createmechanized.content.pilot_helmet.PilotHelmetDyeRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModRecipeSerializers {

    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, CreateMechanizedMod.MODID);

    public static final DeferredHolder<RecipeSerializer<?>, SimpleCraftingRecipeSerializer<PilotHelmetDyeRecipe>> PILOT_HELMET_DYE =
            SERIALIZERS.register(
                    "pilot_helmet_dye",
                    () -> new SimpleCraftingRecipeSerializer<>(PilotHelmetDyeRecipe::new)
            );

    private ModRecipeSerializers() {
    }

    public static void register(IEventBus modEventBus) {
        SERIALIZERS.register(modEventBus);
    }
}
