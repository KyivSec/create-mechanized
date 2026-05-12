package kyivsec.createmechanized.content.pilot_helmet;

import kyivsec.createmechanized.CreateMechanizedConfig;
import kyivsec.createmechanized.registry.ModDataComponents;
import kyivsec.createmechanized.registry.ModItems;
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
import net.minecraft.world.item.ElytraItem;
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

    public static boolean isWornBy(Player player) {
        if (player == null) {
            return false;
        }
        return player.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.PILOT_HELMET.get());
    }

    public static boolean hasElytraEquipped(Player player) {
        if (player == null) {
            return false;
        }
        return player.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof ElytraItem;
    }

    public static int getColorRgb(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !stack.is(ModItems.PILOT_HELMET.get())) {
            return CreateMechanizedConfig.DEFAULT_RGB;
        }
        Integer color = stack.get(ModDataComponents.PILOT_HELMET_COLOR.get());
        return color != null ? (color & 0xFFFFFF) : CreateMechanizedConfig.DEFAULT_RGB;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        Integer stored = stack.get(ModDataComponents.PILOT_HELMET_COLOR.get());
        int rgb = (stored != null) ? (stored & 0xFFFFFF) : CreateMechanizedConfig.DEFAULT_RGB;

        String dyeId = CreateMechanizedConfig.findDyeIdByRgb(rgb);
        String name;
        if (dyeId != null) {
            name = CreateMechanizedConfig.getDisplayName(dyeId);
        } else {
            name = String.format("#%06X", rgb);
        }

        Component prefix = Component.literal("Color: ").withStyle(ChatFormatting.GRAY);
        Component value = Component.literal(name)
                .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(rgb)));
        tooltip.add(Component.empty().append(prefix).append(value));
    }
}
