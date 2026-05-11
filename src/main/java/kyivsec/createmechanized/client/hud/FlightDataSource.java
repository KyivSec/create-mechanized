package kyivsec.createmechanized.client.hud;

import dev.ryanhcode.sable.companion.ClientSubLevelAccess;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import net.minecraft.client.player.LocalPlayer;
import org.joml.Quaterniondc;
import org.joml.Vector3d;
import org.joml.Vector3dc;

/**
 * Builds {@link FlightData} snapshots for the pilot HUD.
 *
 * <p>Two sources are supported:</p>
 * <ul>
 *     <li>{@link #fromPlayer(LocalPlayer)} — minimal HUD data derived from vanilla
 *     player state (altitude only).</li>
 *     <li>{@link #fromSubLevel(ClientSubLevelAccess, float)} — full HUD data derived
 *     from a Sable sublevel pose.</li>
 * </ul>
 *
 * <p>Attitude extraction uses direct basis-vector transformation (rather than
 * quaternion-to-Euler formulas) because it is convention-agnostic: pitch is just
 * the elevation of the forward axis, roll is the bank of the right axis, and
 * heading is the compass azimuth of the forward axis. This avoids guessing
 * Sable's quaternion convention.</p>
 */
public final class FlightDataSource {

    /** Body-frame forward axis at identity orientation (Minecraft convention: -Z is "forward"). */
    private static final Vector3dc BODY_FORWARD = new Vector3d(0.0, 0.0, -1.0);
    /** Body-frame right axis at identity orientation (+X). */
    private static final Vector3dc BODY_RIGHT = new Vector3d(1.0, 0.0, 0.0);

    private FlightDataSource() {
    }

    /** Snapshot using only the vanilla player position. Marks {@link FlightData#minimal()} = true. */
    public static FlightData fromPlayer(LocalPlayer player) {
        return new FlightData(player.getY(), 0.0, 0.0f, 0.0f, 0.0f, 0.0, 0.0, 0.0, true);
    }

    /**
     * Snapshot derived from a Sable sublevel's render pose.
     *
     * <p>Speed = (logical position - last position) × 20 ticks/s, in m/s.</p>
     * <p>Pitch/roll/heading are extracted from the orientation by rotating canonical
     * forward/right unit vectors and reading off geometric quantities.</p>
     */
    public static FlightData fromSubLevel(ClientSubLevelAccess sub, float partialTick) {
        Pose3dc renderPose = sub.renderPose(partialTick);
        Vector3dc renderPos = renderPose.position();
        Quaterniondc orientation = renderPose.orientation();

        // Transform unit basis vectors into world space.
        Vector3d forward = orientation.transform(new Vector3d(BODY_FORWARD));
        Vector3d right = orientation.transform(new Vector3d(BODY_RIGHT));

        // Pitch: arcsin of the vertical component of the forward axis.
        // forward.y > 0 means nose-up.
        float pitchRad = (float) Math.asin(clamp(forward.y, -1.0, 1.0));

        // Heading (compass azimuth): atan2 of forward's horizontal projection.
        // 0 = looking towards -Z (Minecraft "north"), increasing clockwise (toward +X = "east").
        float headingRad = (float) Math.atan2(forward.x, -forward.z);
        if (headingRad < 0.0f) {
            headingRad += (float) (Math.PI * 2.0);
        }

        // Roll: how far the right wing has tilted from horizontal.
        // right.y > 0 means right wing is up → conventional roll is NEGATIVE in that case
        // (right-wing-down positive). Pure asin works because right is unit-length in world space.
        float rollRad = (float) -Math.asin(clamp(right.y, -1.0, 1.0));

        // Velocity: difference between logical and last positions (per tick).
        Vector3dc logical = sub.logicalPose().position();
        Vector3dc last = sub.lastPose().position();
        Vector3d worldVel = new Vector3d(
                logical.x() - last.x(),
                logical.y() - last.y(),
                logical.z() - last.z()
        );
        double metersPerTick = worldVel.length();
        double speedMs = metersPerTick * 20.0;

        // Convert velocity into body frame: bodyVel = orientation^-1 * worldVel.
        // Body-axis convention: +X = right, +Y = up, -Z = forward (matches Minecraft player look).
        Vector3d bodyVel = new Vector3d(worldVel);
        orientation.transformInverse(bodyVel);

        return new FlightData(
                renderPos.y(),
                speedMs,
                pitchRad,
                rollRad,
                headingRad,
                bodyVel.x,
                bodyVel.y,
                bodyVel.z,
                false
        );
    }

    private static double clamp(double v, double lo, double hi) {
        return v < lo ? lo : (v > hi ? hi : v);
    }
}
