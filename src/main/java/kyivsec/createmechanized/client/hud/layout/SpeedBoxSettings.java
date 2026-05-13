package kyivsec.createmechanized.client.hud.layout;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record SpeedBoxSettings(boolean enabled, int leftInset) {

    public static final SpeedBoxSettings DEFAULT = new SpeedBoxSettings(true, 16);

    public static final Codec<SpeedBoxSettings> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.BOOL.optionalFieldOf("enabled", true).forGetter(SpeedBoxSettings::enabled),
            Codec.INT.optionalFieldOf("left_inset", 16).forGetter(SpeedBoxSettings::leftInset)
    ).apply(i, SpeedBoxSettings::new));
}
