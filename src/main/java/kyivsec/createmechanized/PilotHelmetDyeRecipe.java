package kyivsec.createmechanized;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/**
 * Custom shapeless crafting recipe: 1× pilot helmet + 1× dye → tinted pilot helmet.
 *
 * <p>Match rules:</p>
 * <ul>
 *     <li>Exactly one {@link ModItems#PILOT_HELMET} stack on the grid.</li>
 *     <li>Exactly one {@link net.minecraft.world.item.DyeItem} whose color is in
 *     the {@link PilotHelmetColor} palette.</li>
 *     <li>No other items.</li>
 * </ul>
 *
 * <p>The assembled output is a single-count helmet stack with its
 * {@link ModDataComponents#PILOT_HELMET_COLOR} component set to the dye's RGB.</p>
 */
public class PilotHelmetDyeRecipe extends CustomRecipe {

    public PilotHelmetDyeRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        ItemStack helmet = ItemStack.EMPTY;
        PilotHelmetColor color = null;

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) continue;

            if (stack.is(ModItems.PILOT_HELMET.get())) {
                if (!helmet.isEmpty()) return false; // more than one helmet
                helmet = stack;
            } else {
                PilotHelmetColor c = PilotHelmetColor.fromDyeItem(stack.getItem());
                if (c == null) return false;        // unrecognised item
                if (color != null) return false;    // more than one dye
                color = c;
            }
        }
        return !helmet.isEmpty() && color != null;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider provider) {
        ItemStack helmet = ItemStack.EMPTY;
        PilotHelmetColor color = null;

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) continue;
            if (stack.is(ModItems.PILOT_HELMET.get())) {
                helmet = stack;
            } else {
                color = PilotHelmetColor.fromDyeItem(stack.getItem());
            }
        }
        if (helmet.isEmpty() || color == null) return ItemStack.EMPTY;

        ItemStack result = helmet.copyWithCount(1);
        result.set(ModDataComponents.PILOT_HELMET_COLOR.get(), color.rgb);
        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.PILOT_HELMET_DYE.get();
    }
}
