package kyivsec.createmechanized.client.hud.layout;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record PitchLadderSettings(boolean enabled, float pixelsPerRad, int fadeHalfPx, int stepDeg, int maxDeg) {

    public static final PitchLadderSettings DEFAULT = new PitchLadderSettings(true, 200.0f, 95, 5, 90);

    public static final Codec<PitchLadderSettings> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.BOOL.optionalFieldOf("enabled", true).forGetter(PitchLadderSettings::enabled),
            Codec.FLOAT.optionalFieldOf("pixels_per_rad", 200.0f).forGetter(PitchLadderSettings::pixelsPerRad),
            Codec.INT.optionalFieldOf("fade_half_px", 95).forGetter(PitchLadderSettings::fadeHalfPx),
            Codec.INT.optionalFieldOf("step_deg", 5).forGetter(PitchLadderSettings::stepDeg),
            Codec.INT.optionalFieldOf("max_deg", 90).forGetter(PitchLadderSettings::maxDeg)
    ).apply(i, PitchLadderSettings::new));
}
