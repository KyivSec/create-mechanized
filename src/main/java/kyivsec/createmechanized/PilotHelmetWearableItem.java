package kyivsec.createmechanized;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class PilotHelmetWearableItem extends Item implements Equipable {
    public PilotHelmetWearableItem(Properties properties) {
        super(properties);
    }

    @Override
    public EquipmentSlot getEquipmentSlot() {
        return EquipmentSlot.HEAD;
    }

    @Override
    public Holder<SoundEvent> getEquipSound() {
        return SoundEvents.ARMOR_EQUIP_IRON;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return this.swapWithEquipmentSlot(this, level, player, hand);
    }

    /**
     * Returns {@code true} if the given player is currently wearing the pilot helmet in their head slot.
     * Safe to call with a {@code null} player (returns {@code false}).
     */
    public static boolean isWornBy(Player player) {
        if (player == null) {
            return false;
        }
        return player.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.PILOT_HELMET.get());
    }

    /**
     * Reads the HUD tint color from a helmet stack's {@link ModDataComponents#PILOT_HELMET_COLOR}
     * component. Falls back to {@link PilotHelmetColor#DEFAULT_RGB} (the original HUD green) when the
     * stack is empty, isn't a pilot helmet, or has no color component set.
     *
     * @return RGB as 0xRRGGBB (alpha must be supplied by the caller)
     */
    public static int getColorRgb(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !stack.is(ModItems.PILOT_HELMET.get())) {
            return PilotHelmetColor.DEFAULT_RGB;
        }
        Integer color = stack.get(ModDataComponents.PILOT_HELMET_COLOR.get());
        return color != null ? (color & 0xFFFFFF) : PilotHelmetColor.DEFAULT_RGB;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        Integer rgb = stack.get(ModDataComponents.PILOT_HELMET_COLOR.get());
        if (rgb == null) {
            return;
        }
        PilotHelmetColor entry = PilotHelmetColor.fromRgb(rgb);
        String name = (entry != null) ? entry.displayName : String.format("#%06X", rgb & 0xFFFFFF);

        Component prefix = Component.literal("Color: ").withStyle(ChatFormatting.GRAY);
        Component value = Component.literal(name)
                .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(rgb & 0xFFFFFF)));
        tooltip.add(Component.empty().append(prefix).append(value));
    }
}
