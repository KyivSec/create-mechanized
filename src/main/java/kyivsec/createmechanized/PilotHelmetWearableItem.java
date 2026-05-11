package kyivsec.createmechanized;

import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

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
}
