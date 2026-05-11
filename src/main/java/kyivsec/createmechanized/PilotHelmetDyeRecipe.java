package kyivsec.createmechanized;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

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
                if (!helmet.isEmpty()) return false;
                helmet = stack;
            } else {
                PilotHelmetColor c = PilotHelmetColor.fromDyeItem(stack.getItem());
                if (c == null) return false;
                if (color != null) return false;
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
