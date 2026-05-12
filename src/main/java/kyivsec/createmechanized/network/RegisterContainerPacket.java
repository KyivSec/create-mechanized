package kyivsec.createmechanized.network;

import io.netty.buffer.ByteBuf;
import kyivsec.createmechanized.CreateMechanizedMod;
import kyivsec.createmechanized.content.pilot_helmet.TrackedContainer;
import kyivsec.createmechanized.content.pilot_helmet.TrackedContainerList;
import kyivsec.createmechanized.registry.ModDataComponents;
import kyivsec.createmechanized.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Optional;
import java.util.UUID;

public record RegisterContainerPacket(BlockPos pos, Optional<UUID> sublevelId, String name, TrackedContainer.Kind kind)
        implements CustomPacketPayload {

    public static final Type<RegisterContainerPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(CreateMechanizedMod.MODID, "register_container"));

    public static final StreamCodec<ByteBuf, RegisterContainerPacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, RegisterContainerPacket::pos,
            ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC), RegisterContainerPacket::sublevelId,
            ByteBufCodecs.STRING_UTF8, RegisterContainerPacket::name,
            TrackedContainer.Kind.STREAM_CODEC, RegisterContainerPacket::kind,
            RegisterContainerPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(RegisterContainerPacket payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
            if (!stack.is(ModItems.PILOT_HELMET.get())) {
                return;
            }
            Level level = player.level();
            boolean ok = switch (payload.kind) {
                case FLUID -> level.getCapability(Capabilities.FluidHandler.BLOCK, payload.pos, null) != null;
                case ENERGY -> level.getCapability(Capabilities.EnergyStorage.BLOCK, payload.pos, null) != null;
            };
            if (!ok) {
                return;
            }
            String name = payload.name == null ? "" : payload.name.trim();
            if (name.isEmpty() || name.length() > 32) {
                return;
            }
            TrackedContainerList current = stack.getOrDefault(
                    ModDataComponents.TRACKED_CONTAINERS.get(), TrackedContainerList.EMPTY);
            TrackedContainerList updated = current.add(
                    new TrackedContainer(payload.pos, payload.sublevelId, name, payload.kind));
            stack.set(ModDataComponents.TRACKED_CONTAINERS.get(), updated);
        });
    }
}
