package kyivsec.createmechanized.client.hud.layout;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record BankScaleSettings(boolean enabled, int radius, int halfDeg) {

    public static final BankScaleSettings DEFAULT = new BankScaleSettings(true, 78, 60);

    public static final Codec<BankScaleSettings> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.BOOL.optionalFieldOf("enabled", true).forGetter(BankScaleSettings::enabled),
            Codec.INT.optionalFieldOf("radius", 78).forGetter(BankScaleSettings::radius),
            Codec.INT.optionalFieldOf("half_deg", 60).forGetter(BankScaleSettings::halfDeg)
    ).apply(i, BankScaleSettings::new));
}
