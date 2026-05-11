package kyivsec.createmechanized.client.hud;

import dev.ryanhcode.sable.companion.ClientSubLevelAccess;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import net.minecraft.client.player.LocalPlayer;
import org.joml.Quaterniondc;
import org.joml.Vector3d;
import org.joml.Vector3dc;

public final class FlightDataSource {

    private static final Vector3dc BODY_FORWARD = new Vector3d(0.0, 0.0, -1.0);
    private static final Vector3dc BODY_RIGHT = new Vector3d(1.0, 0.0, 0.0);

    private FlightDataSource() {
    }

    public static FlightData fromPlayer(LocalPlayer player) {
        return new FlightData(player.getY(), 0.0, 0.0f, 0.0f, 0.0f, 0.0, 0.0, 0.0, true);
    }

    public static FlightData fromSubLevel(ClientSubLevelAccess sub, float partialTick) {
        Pose3dc renderPose = sub.renderPose(partialTick);
        Vector3dc renderPos = renderPose.position();
        Quaterniondc orientation = renderPose.orientation();

        Vector3d forward = orientation.transform(new Vector3d(BODY_FORWARD));
        Vector3d right = orientation.transform(new Vector3d(BODY_RIGHT));

        float pitchRad = (float) Math.asin(clamp(forward.y, -1.0, 1.0));

        float headingRad = (float) Math.atan2(forward.x, -forward.z);
        if (headingRad < 0.0f) {
            headingRad += (float) (Math.PI * 2.0);
        }

        float rollRad = (float) -Math.asin(clamp(right.y, -1.0, 1.0));

        Vector3dc logical = sub.logicalPose().position();
        Vector3dc last = sub.lastPose().position();
        Vector3d worldVel = new Vector3d(
                logical.x() - last.x(),
                logical.y() - last.y(),
                logical.z() - last.z()
        );
        double metersPerTick = worldVel.length();
        double speedMs = metersPerTick * 20.0;

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
