package kyivsec.createmechanized.client.hud.layout;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record HeadingTapeSettings(boolean enabled, int tapeY, int pxPerDeg, int halfWidthPx) {

    public static final HeadingTapeSettings DEFAULT = new HeadingTapeSettings(true, 21, 4, 60);

    public static final Codec<HeadingTapeSettings> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.BOOL.optionalFieldOf("enabled", true).forGetter(HeadingTapeSettings::enabled),
            Codec.INT.optionalFieldOf("tape_y", 21).forGetter(HeadingTapeSettings::tapeY),
            Codec.INT.optionalFieldOf("px_per_deg", 4).forGetter(HeadingTapeSettings::pxPerDeg),
            Codec.INT.optionalFieldOf("half_width_px", 60).forGetter(HeadingTapeSettings::halfWidthPx)
    ).apply(i, HeadingTapeSettings::new));
}
