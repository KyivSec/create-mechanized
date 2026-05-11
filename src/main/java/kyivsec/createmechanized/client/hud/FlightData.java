package kyivsec.createmechanized.client.hud;

/**
 * Immutable snapshot of flight data shown on the pilot HUD for one frame.
 *
 * <p>When {@link #minimal} is {@code true} the player is NOT on a Sable sublevel; only
 * {@link #altitude} is meaningful — speed/pitch/roll/heading are zero and the renderer
 * should omit the corresponding HUD widgets.</p>
 *
 * @param altitude    meters above world Y=0 (or the sublevel's logical Y when on one)
 * @param speedMs     tangential speed in meters per second (Sable's native unit)
 * @param pitchRad    pitch (nose-up positive) in radians, extracted from the body's forward vector
 * @param rollRad     roll (right-wing-down positive) in radians
 * @param headingRad  compass heading in radians (0 = north, positive = clockwise)
 * @param bodyVelX    velocity along body-right axis (m/tick) — drives FPM horizontal offset
 * @param bodyVelY    velocity along body-up axis    (m/tick) — drives FPM vertical offset
 * @param bodyVelZ    velocity along body-forward axis (m/tick) — used for FPM angle math
 * @param minimal     true when the data came from the player (not a sublevel) and only altitude+boresight should be drawn
 */
public record FlightData(
        double altitude,
        double speedMs,
        float pitchRad,
        float rollRad,
        float headingRad,
        double bodyVelX,
        double bodyVelY,
        double bodyVelZ,
        boolean minimal
) {
}
