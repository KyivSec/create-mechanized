package kyivsec.createmechanized.client.hud;

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
