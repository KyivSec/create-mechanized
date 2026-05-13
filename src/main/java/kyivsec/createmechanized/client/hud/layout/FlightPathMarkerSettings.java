package kyivsec.createmechanized.client.hud.layout;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record FlightPathMarkerSettings(boolean enabled, int radius, double maxDriftDeg) {

    public static final FlightPathMarkerSettings DEFAULT = new FlightPathMarkerSettings(true, 6, 15.0);

    public static final Codec<FlightPathMarkerSettings> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.BOOL.optionalFieldOf("enabled", true).forGetter(FlightPathMarkerSettings::enabled),
            Codec.INT.optionalFieldOf("radius", 6).forGetter(FlightPathMarkerSettings::radius),
            Codec.DOUBLE.optionalFieldOf("max_drift_deg", 15.0).forGetter(FlightPathMarkerSettings::maxDriftDeg)
    ).apply(i, FlightPathMarkerSettings::new));
}
