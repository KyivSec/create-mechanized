package kyivsec.createmechanized.content.pilot_helmet;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Optional;
import java.util.UUID;

public record TrackedContainer(BlockPos pos, Optional<UUID> sublevelId, String name, Kind kind) {

    public enum Kind {
        FLUID, ENERGY;

        public static final Codec<Kind> CODEC = Codec.STRING.xmap(
                s -> Kind.valueOf(s.toUpperCase()),
                Kind::name
        );

        public static final StreamCodec<ByteBuf, Kind> STREAM_CODEC =
                ByteBufCodecs.BYTE.map(b -> Kind.values()[b & 0xFF], k -> (byte) k.ordinal());
    }

    public static final Codec<TrackedContainer> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            BlockPos.CODEC.fieldOf("pos").forGetter(TrackedContainer::pos),
            UUIDUtil.CODEC.optionalFieldOf("sublevelId").forGetter(TrackedContainer::sublevelId),
            Codec.STRING.fieldOf("name").forGetter(TrackedContainer::name),
            Kind.CODEC.fieldOf("kind").forGetter(TrackedContainer::kind)
    ).apply(inst, TrackedContainer::new));

    public static final StreamCodec<ByteBuf, TrackedContainer> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, TrackedContainer::pos,
            ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC), TrackedContainer::sublevelId,
            ByteBufCodecs.STRING_UTF8, TrackedContainer::name,
            Kind.STREAM_CODEC, TrackedContainer::kind,
            TrackedContainer::new
    );
}
