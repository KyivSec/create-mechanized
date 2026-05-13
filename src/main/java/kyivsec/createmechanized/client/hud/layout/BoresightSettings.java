package kyivsec.createmechanized.client.hud.layout;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record BoresightSettings(boolean enabled) {

    public static final BoresightSettings DEFAULT = new BoresightSettings(true);

    public static final Codec<BoresightSettings> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.BOOL.optionalFieldOf("enabled", true).forGetter(BoresightSettings::enabled)
    ).apply(i, BoresightSettings::new));
}
