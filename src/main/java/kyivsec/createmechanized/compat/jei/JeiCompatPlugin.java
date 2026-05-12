package kyivsec.createmechanized.compat.jei;

import kyivsec.createmechanized.CreateMechanizedConfig;
import kyivsec.createmechanized.CreateMechanizedMod;
import kyivsec.createmechanized.registry.ModDataComponents;
import kyivsec.createmechanized.registry.ModItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@JeiPlugin
public class JeiCompatPlugin implements IModPlugin {

    private static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(CreateMechanizedMod.MODID, "jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return ID;
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        ItemStack baseHelmet = new ItemStack(ModItems.PILOT_HELMET.get());

        List<RecipeHolder<CraftingRecipe>> recipes = new ArrayList<>(CreateMechanizedConfig.HELMET_DYE_COLORS.size());
        for (Map.Entry<String, ModConfigSpec.ConfigValue<String>> e : CreateMechanizedConfig.HELMET_DYE_COLORS.entrySet()) {
            String dyeId = e.getKey();
            Integer rgb = CreateMechanizedConfig.getColorRgb(dyeId);
            if (rgb == null) continue;

            ResourceLocation rl = ResourceLocation.tryParse(dyeId);
            if (rl == null) continue;
            Item dyeItem = BuiltInRegistries.ITEM.get(rl);
            if (dyeItem == Items.AIR) continue;

            ItemStack dyeStack = new ItemStack(dyeItem);
            ItemStack result = baseHelmet.copy();
            result.set(ModDataComponents.PILOT_HELMET_COLOR.get(), rgb);

            NonNullList<Ingredient> ingredients = NonNullList.of(
                    Ingredient.EMPTY,
                    Ingredient.of(baseHelmet),
                    Ingredient.of(dyeStack)
            );
            ShapelessRecipe shapeless = new ShapelessRecipe(
                    "",
                    CraftingBookCategory.EQUIPMENT,
                    result,
                    ingredients
            );

            ResourceLocation recipeId = ResourceLocation.fromNamespaceAndPath(
                    CreateMechanizedMod.MODID,
                    "jei_pilot_helmet_dye_" + rl.getPath()
            );
            recipes.add(new RecipeHolder<>(recipeId, shapeless));
        }

        registration.addRecipes(RecipeTypes.CRAFTING, recipes);
    }
}
