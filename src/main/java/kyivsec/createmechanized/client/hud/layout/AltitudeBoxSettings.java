package kyivsec.createmechanized.client.hud.layout;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record AltitudeBoxSettings(boolean enabled, int rightInset) {

    public static final AltitudeBoxSettings DEFAULT = new AltitudeBoxSettings(true, 16);

    public static final Codec<AltitudeBoxSettings> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.BOOL.optionalFieldOf("enabled", true).forGetter(AltitudeBoxSettings::enabled),
            Codec.INT.optionalFieldOf("right_inset", 16).forGetter(AltitudeBoxSettings::rightInset)
    ).apply(i, AltitudeBoxSettings::new));
}
