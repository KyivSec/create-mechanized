package kyivsec.createmechanized.client.hud.layout;

public record HudLayout(
        BoresightSettings boresight,
        AltitudeBoxSettings altitudeBox,
        SpeedBoxSettings speedBox,
        PitchLadderSettings pitchLadder,
        BankScaleSettings bankScale,
        HeadingTapeSettings headingTape,
        FlightPathMarkerSettings flightPathMarker
) {
    public static final HudLayout DEFAULT = new HudLayout(
            BoresightSettings.DEFAULT,
            AltitudeBoxSettings.DEFAULT,
            SpeedBoxSettings.DEFAULT,
            PitchLadderSettings.DEFAULT,
            BankScaleSettings.DEFAULT,
            HeadingTapeSettings.DEFAULT,
            FlightPathMarkerSettings.DEFAULT
    );
}
