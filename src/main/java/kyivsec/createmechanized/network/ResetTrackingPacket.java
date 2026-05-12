package kyivsec.createmechanized.network;

import io.netty.buffer.ByteBuf;
import kyivsec.createmechanized.CreateMechanizedMod;
import kyivsec.createmechanized.registry.ModDataComponents;
import kyivsec.createmechanized.registry.ModItems;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ResetTrackingPacket() implements CustomPacketPayload {

    public static final ResetTrackingPacket INSTANCE = new ResetTrackingPacket();

    public static final Type<ResetTrackingPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(CreateMechanizedMod.MODID, "reset_tracking"));

    public static final StreamCodec<ByteBuf, ResetTrackingPacket> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ResetTrackingPacket payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
            if (!stack.is(ModItems.PILOT_HELMET.get())) {
                return;
            }
            stack.remove(ModDataComponents.TRACKED_CONTAINERS.get());
        });
    }
}
