package cinematic.snowstorm.effects;

import cinematic.snowstorm.config.SnowfallConfig;
import cinematic.snowstorm.particle.ParticleTypes;
import cinematic.snowstorm.utils.SnowSpawnManager;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;

/**
 * Speed streaks: while snowing and the player moves faster than a threshold (elytra, boats,
 * falling, horses...), stretched streak particles rush past the camera.
 * Streaks stand still in the world; the player's own motion makes them streak by.
 * Everything is placed relative to the render camera (not the player's eyes), so it also works in
 * third person: there the camera is behind or in front of the player and looks the other way.
 * The camera is rebuilt from the player and the camera type instead of read from GameRenderer,
 * because that accessor was renamed between versions (getMainCamera -> mainCamera in 26.2).
 */
public final class SpeedStreaks {
    // +1: a positive particle roll turns the sprite counter-clockwise on screen.
    // If streaks lean the wrong way in game, set this to -1.
    private static final float ROLL_SIGN = 1.0f;

    // Half-size of the spawn area on screen, as a share of the depth (tangent of the half angle).
    private static final double VIEW_HALF_WIDTH = 0.9;
    private static final double VIEW_HALF_HEIGHT = 0.6;

    // Vanilla third-person camera distance. Wall clipping is ignored: a few blocks off is invisible here.
    private static final double THIRD_PERSON_DISTANCE = 4.0;

    private static final double BASE_RATE = 8.0;   // streaks per tick at density 1.0, full speed
    private static final int MAX_PER_TICK = 24;

    // Camera basis (right / up / forward) and eye position, refreshed every tick while active.
    private static double ex, ey, ez;
    private static double rx, ry, rz;
    private static double ux, uy, uz;
    private static double fx, fy, fz;
    private static double vx, vy, vz;
    private static boolean hasBasis;
    private static float accumulator;

    private SpeedStreaks() {}

    public static void tick(ClientLevel level, LocalPlayer player, boolean snowing) {
        if (!SnowfallConfig.ENABLE_SPEED_STREAKS || !snowing || player == null || player.isSpectator()) {
            accumulator = 0f;
            return;
        }
        Vec3 vel = SnowSpawnManager.getPlayerVelocity();
        double speed = vel.length();
        double threshold = Math.max(0.05, SnowfallConfig.STREAK_SPEED_THRESHOLD);
        if (speed <= threshold || !level.canSeeSky(player.blockPosition())) {
            accumulator = 0f;
            return;
        }

        // Front view looks back at the player: yaw + 180, pitch inverted. Otherwise same as the player.
        CameraType cameraType = Minecraft.getInstance().options.getCameraType();
        boolean front = cameraType == CameraType.THIRD_PERSON_FRONT;
        double yaw = Math.toRadians(player.getYRot() + (front ? 180.0 : 0.0));
        double pitch = Math.toRadians(front ? -player.getXRot() : player.getXRot());
        fx = -Math.sin(yaw) * Math.cos(pitch);
        fy = -Math.sin(pitch);
        fz = Math.cos(yaw) * Math.cos(pitch);
        rx = -Math.cos(yaw);
        ry = 0.0;
        rz = -Math.sin(yaw);
        // up = right x forward
        ux = ry * fz - rz * fy;
        uy = rz * fx - rx * fz;
        uz = rx * fy - ry * fx;
        // Third person: the camera sits that far back along its own view direction.
        Vec3 eye = player.getEyePosition();
        double back = cameraType.isFirstPerson() ? 0.0 : THIRD_PERSON_DISTANCE;
        ex = eye.x - fx * back; ey = eye.y - fy * back; ez = eye.z - fz * back;
        vx = vel.x; vy = vel.y; vz = vel.z;
        hasBasis = true;

        // 0 at the threshold, 1 at twice the threshold; never fully zero just above it.
        double factor = Math.min(1.0, (speed - threshold) / threshold);
        accumulator += (float) (BASE_RATE * Math.max(0.0, SnowfallConfig.STREAK_DENSITY) * (0.25 + 0.75 * factor));
        int count = (int) accumulator;
        accumulator -= count;
        count = Math.min(count, MAX_PER_TICK);

        double clearance = SnowfallConfig.STREAK_CENTER_CLEARANCE;
        for (int i = 0; i < count; i++) {
            // A random point inside the view frustum, 2.5..12.5 blocks in front of the camera,
            // outside the cleared area in the middle of the screen (a few tries, then give up).
            double d = 0.0, a = 0.0, b = 0.0;
            boolean found = false;
            for (int attempt = 0; attempt < 4 && !found; attempt++) {
                d = 2.5 + level.getRandom().nextDouble() * 10.0;
                double nx = level.getRandom().nextDouble() * 2.0 - 1.0;
                double ny = level.getRandom().nextDouble() * 2.0 - 1.0;
                a = nx * d * VIEW_HALF_WIDTH;
                b = ny * d * VIEW_HALF_HEIGHT;
                found = Math.hypot(nx, ny) >= clearance;
            }
            if (!found) continue;
            level.addParticle(ParticleTypes.MY_SNOWSTREAK,
                    ex + fx * d + rx * a + ux * b,
                    ey + fy * d + ry * a + uy * b,
                    ez + fz * d + rz * a + uz * b,
                    0.0, 0.0, 0.0);
        }
    }

    /**
     * 0..1 visibility from the position on screen: 0 in the cleared middle of the screen,
     * fading in to 1 over the next 20% of the view radius.
     */
    public static float centerFade(double x, double y, double z) {
        double clearance = SnowfallConfig.STREAK_CENTER_CLEARANCE;
        if (!hasBasis || clearance <= 0.0) return 1.0f;
        double qx = x - ex, qy = y - ey, qz = z - ez;
        double cz = Math.max(0.1, qx * fx + qy * fy + qz * fz);
        double nx = (qx * rx + qy * ry + qz * rz) / cz / VIEW_HALF_WIDTH;
        double ny = (qx * ux + qy * uy + qz * uz) / cz / VIEW_HALF_HEIGHT;
        double t = (Math.hypot(nx, ny) - clearance) / 0.2;
        return (float) Math.max(0.0, Math.min(1.0, t));
    }

    /** True once a streak is behind the player's eye (the player has flown past it). */
    public static boolean isBehind(double x, double y, double z) {
        if (!hasBasis) return false;
        return (x - ex) * fx + (y - ey) * fy + (z - ez) * fz < 0.0;
    }

    /**
     * Sprite rotation that lines a streak up with the direction it appears to move on screen.
     * The sprite's long axis is vertical, so 0 = pointing up.
     */
    public static float streakRoll(double x, double y, double z) {
        if (!hasBasis) return 0.0f;
        double qx = x - ex, qy = y - ey, qz = z - ez;
        // Point in camera space (x right, y up, z depth).
        double cx = qx * rx + qy * ry + qz * rz;
        double cy = qx * ux + qy * uy + qz * uz;
        double cz = Math.max(0.1, qx * fx + qy * fy + qz * fz);
        // The world is static, so relative to the camera the point moves with -velocity.
        double wx = -(vx * rx + vy * ry + vz * rz);
        double wy = -(vx * ux + vy * uy + vz * uz);
        double wz = -(vx * fx + vy * fy + vz * fz);
        // Screen-space velocity of a perspective-projected point (common 1/z^2 factor dropped).
        double dirX = wx * cz - cx * wz;
        double dirY = wy * cz - cy * wz;
        if (dirX * dirX + dirY * dirY < 1.0e-12) return 0.0f;
        return (float) (ROLL_SIGN * Math.atan2(-dirX, dirY));
    }
}
