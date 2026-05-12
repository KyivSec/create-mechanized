package kyivsec.createmechanized.content.pilot_helmet;

import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.companion.SubLevelAccess;
import kyivsec.createmechanized.CreateMechanizedConfig;
import kyivsec.createmechanized.content.pilot_helmet.client.ContainerNameScreen;
import kyivsec.createmechanized.network.ResetTrackingPacket;
import kyivsec.createmechanized.registry.ModDataComponents;
import kyivsec.createmechanized.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
        ItemStack stack = player.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResultHolder.pass(stack);
        }
        if (player.isShiftKeyDown()) {
            if (level.isClientSide) {
                PacketDistributor.sendToServer(ResetTrackingPacket.INSTANCE);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }
        return this.swapWithEquipmentSlot(this, level, player, hand);
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Player player = ctx.getPlayer();
        if (player == null || !player.isShiftKeyDown() || ctx.getHand() != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        Level level = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        TrackedContainer.Kind kind = detectKind(level, pos);
        if (kind == null) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            Optional<UUID> sublevelId = resolveSublevel(level, pos);
            Minecraft.getInstance().setScreen(new ContainerNameScreen(pos, sublevelId, kind));
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private static TrackedContainer.Kind detectKind(Level level, BlockPos pos) {
        if (level.getCapability(Capabilities.FluidHandler.BLOCK, pos, null) != null) {
            return TrackedContainer.Kind.FLUID;
        }
        if (level.getCapability(Capabilities.EnergyStorage.BLOCK, pos, null) != null) {
            return TrackedContainer.Kind.ENERGY;
        }
        return null;
    }

    public static Optional<UUID> resolveSublevel(Level level, BlockPos pos) {
        try {
            SubLevelAccess access = Sable.HELPER.getContaining(level, pos);
            return access == null ? Optional.empty() : Optional.of(access.getUniqueId());
        } catch (Throwable t) {
            return Optional.empty();
        }
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
        String colorName = (dyeId != null) ? CreateMechanizedConfig.getDisplayName(dyeId) : String.format("#%06X", rgb);

        Component prefix = Component.literal("Color: ").withStyle(ChatFormatting.GRAY);
        Component value = Component.literal(colorName)
                .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(rgb)));
        tooltip.add(Component.empty().append(prefix).append(value));

        TrackedContainerList containers = stack.get(ModDataComponents.TRACKED_CONTAINERS.get());
        if (containers != null && !containers.entries().isEmpty()) {
            for (TrackedContainer c : containers.entries()) {
                tooltip.add(Component.literal("• " + c.name() + " (" + c.kind().name() + ")")
                        .withStyle(ChatFormatting.DARK_GRAY));
            }
        }
    }
}
