package kyivsec.createmechanized.content.pilot_helmet;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayList;
import java.util.List;

public record TrackedContainerList(List<TrackedContainer> entries) {

    public static final int CAP = 18;
    public static final TrackedContainerList EMPTY = new TrackedContainerList(List.of());

    public static final Codec<TrackedContainerList> CODEC = TrackedContainer.CODEC.listOf()
            .xmap(TrackedContainerList::new, TrackedContainerList::entries);

    public static final StreamCodec<ByteBuf, TrackedContainerList> STREAM_CODEC =
            TrackedContainer.STREAM_CODEC.apply(ByteBufCodecs.list())
                    .map(TrackedContainerList::new, TrackedContainerList::entries);

    public TrackedContainerList add(TrackedContainer entry) {
        if (entries.size() >= CAP) return this;
        List<TrackedContainer> copy = new ArrayList<>(entries);
        copy.add(entry);
        return new TrackedContainerList(List.copyOf(copy));
    }
}
