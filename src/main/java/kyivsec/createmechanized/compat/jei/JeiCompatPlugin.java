package kyivsec.createmechanized.compat.jei;

import kyivsec.createmechanized.CreateMechanizedMod;
import kyivsec.createmechanized.ModDataComponents;
import kyivsec.createmechanized.ModItems;
import kyivsec.createmechanized.PilotHelmetColor;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapelessRecipe;

import java.util.ArrayList;
import java.util.List;

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

        List<RecipeHolder<CraftingRecipe>> recipes = new ArrayList<>(PilotHelmetColor.values().length);
        for (PilotHelmetColor color : PilotHelmetColor.values()) {
            DyeColor dyeColor = DyeColor.byName(color.id, DyeColor.WHITE);
            ItemStack dyeStack = new ItemStack(DyeItem.byColor(dyeColor));

            ItemStack result = baseHelmet.copy();
            result.set(ModDataComponents.PILOT_HELMET_COLOR.get(), color.rgb);

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

            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(
                    CreateMechanizedMod.MODID,
                    "jei_pilot_helmet_dye_" + color.id
            );
            recipes.add(new RecipeHolder<>(id, shapeless));
        }

        registration.addRecipes(RecipeTypes.CRAFTING, recipes);
    }
}
