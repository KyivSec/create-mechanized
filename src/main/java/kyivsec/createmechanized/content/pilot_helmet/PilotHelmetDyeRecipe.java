package kyivsec.createmechanized.content.pilot_helmet;

import kyivsec.createmechanized.CreateMechanizedConfig;
import kyivsec.createmechanized.registry.ModDataComponents;
import kyivsec.createmechanized.registry.ModItems;
import kyivsec.createmechanized.registry.ModRecipeSerializers;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
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
        Integer color = null;

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) continue;

            if (stack.is(ModItems.PILOT_HELMET.get())) {
                if (!helmet.isEmpty()) return false;
                helmet = stack;
            } else {
                Integer c = lookupColor(stack);
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
        Integer color = null;

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) continue;
            if (stack.is(ModItems.PILOT_HELMET.get())) {
                helmet = stack;
            } else {
                Integer c = lookupColor(stack);
                if (c != null) color = c;
            }
        }
        if (helmet.isEmpty() || color == null) return ItemStack.EMPTY;

        ItemStack result = helmet.copyWithCount(1);
        result.set(ModDataComponents.PILOT_HELMET_COLOR.get(), color);
        return result;
    }

    private static Integer lookupColor(ItemStack stack) {
        String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        return CreateMechanizedConfig.getColorRgb(itemId);
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
