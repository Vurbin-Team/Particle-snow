package cinematic.snowstorm.utils;

import cinematic.snowstorm.particle.ParticleTypes;
import cinematic.snowstorm.particle.MySnowflakeParticle;
import cinematic.snowstorm.config.SnowfallConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

public class SnowSpawnManager {
    // Spawn area configuration - INCREASED FOR CINEMATIC EFFECT
    private static int SPAWN_HEIGHT_ABOVE = SnowfallConfig.SPAWN_HEIGHT;  // Height above player
    private static int SPAWN_RADIUS_HORIZONTAL = SnowfallConfig.SPAWN_RADIUS;  // Horizontal radius (was 45)
    private static int SPAWN_COUNT_PER_TICK = SnowfallConfig.PARTICLES_PER_TICK;  // Particles per tick (was 100)
    private static int TICKS_BETWEEN_SPAWN = SnowfallConfig.SPAWN_INTERVAL;

    // Far distance spawning for atmospheric effect
    private static int FAR_SPAWN_RADIUS = SnowfallConfig.FAR_SPAWN_RADIUS;  // Maximum spawn distance
    private static float FAR_SPAWN_CHANCE = SnowfallConfig.FAR_SPAWN_CHANCE;  // 30% of particles spawn far away

    // Player motion tracking
    private static Vec3 lastPlayerPos = Vec3.ZERO;
    private static Vec3 playerVelocity = Vec3.ZERO;
    private static final float VELOCITY_SMOOTHING = 0.3f;

    private static int tickCounter = 0;
    private static boolean isSnowWeatherActive = false;

    public static void init() {
        ClientTickEvents.END_LEVEL_TICK.register(world -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;

            // Track player velocity for prediction
            Vec3 currentPos = mc.player.position();
            Vec3 currentVelocity = currentPos.subtract(lastPlayerPos);

            // Smooth velocity to avoid jitter
            playerVelocity = playerVelocity.scale(1.0 - VELOCITY_SMOOTHING)
                    .add(currentVelocity.scale(VELOCITY_SMOOTHING));

            lastPlayerPos = currentPos;

            // Check weather conditions
            boolean shouldSnow = world.isRaining() || world.isThundering();
            isSnowWeatherActive = shouldSnow;

            if (shouldSnow) {
                tickCounter++;
                if (tickCounter < TICKS_BETWEEN_SPAWN) return;
                tickCounter = 0;

                // Get player position with velocity prediction
                double px = mc.player.getX() + playerVelocity.x * 5; // Predict ahead
                double py = mc.player.getY();
                double pz = mc.player.getZ() + playerVelocity.z * 5;

                // Spawn particles in a circle around and above the player
                for (int i = 0; i < SPAWN_COUNT_PER_TICK; i++) {
                    // Decide if this particle should spawn far away for atmospheric effect
                    boolean spawnFar = world.getRandom().nextFloat() < FAR_SPAWN_CHANCE;
                    int maxRadius = spawnFar ? FAR_SPAWN_RADIUS : SPAWN_RADIUS_HORIZONTAL;

                    float averageFallSpeed = Math.max(
                            0.001f,
                            (Math.max(0.0f, SnowfallConfig.FALL_SPEED_MIN)
                                    + Math.max(0.0f, SnowfallConfig.FALL_SPEED_MAX)) * 0.5f
                    );
                    int estimatedFallTicks = (int) Math.ceil(SPAWN_HEIGHT_ABOVE / averageFallSpeed);
                    Vec3 windDrift = MySnowflakeParticle.getExpectedWindDrift(estimatedFallTicks);

                    // Sample a circular disk perpendicular to the combined wind-and-fall direction.
                    double diskAngle = world.getRandom().nextDouble() * Math.PI * 2;
                    double diskRadius = Math.sqrt(world.getRandom().nextDouble()) * maxRadius;
                    double diskX = Math.cos(diskAngle) * diskRadius;
                    double diskY = Math.sin(diskAngle) * diskRadius;
                    double windAngle = Math.toRadians(SnowfallConfig.WIND_ANGLE);
                    double windStrength = Math.max(0.0, SnowfallConfig.WIND_STRENGTH);
                    double directionLength = Math.sqrt(windStrength * windStrength
                            + averageFallSpeed * averageFallSpeed);
                    double windX = Math.cos(windAngle);
                    double windZ = Math.sin(windAngle);

                    // The disk's first axis is perpendicular to horizontal wind.
                    double crosswindX = -windZ;
                    double crosswindZ = windX;
                    // The second axis completes a circular disk facing along the particle trajectory.
                    double planeX = averageFallSpeed * windX / directionLength;
                    double planeY = windStrength / directionLength;
                    double planeZ = averageFallSpeed * windZ / directionLength;

                    double dx = px - windDrift.x + diskX * crosswindX + diskY * planeX;
                    double dy = py + SPAWN_HEIGHT_ABOVE + diskY * planeY;
                    double dz = pz - windDrift.z + diskX * crosswindZ + diskY * planeZ;

                    // Add slight initial velocity matching player movement
                    double vx = playerVelocity.x * 0.5;
                    double vz = playerVelocity.z * 0.5;

                    // Use alwaysSpawn flag to force rendering at distance
                    world.addParticle(
                            ParticleTypes.getActiveSnowType(),
                            true,
                            true,
                            dx, dy, dz,
                            vx, 0, vz
                    );
                }
            }
        });
    }

    public static void reload() {
        loadConfigValues();

        tickCounter = 0;
        lastPlayerPos = Vec3.ZERO;
        playerVelocity = Vec3.ZERO;
    }

    /**
     * Load values from SnowfallConfig
     */
    private static void loadConfigValues() {
        SPAWN_HEIGHT_ABOVE = SnowfallConfig.SPAWN_HEIGHT;
        SPAWN_RADIUS_HORIZONTAL = SnowfallConfig.SPAWN_RADIUS;
        SPAWN_COUNT_PER_TICK = SnowfallConfig.PARTICLES_PER_TICK;
        TICKS_BETWEEN_SPAWN = SnowfallConfig.SPAWN_INTERVAL;
        FAR_SPAWN_RADIUS = SnowfallConfig.FAR_SPAWN_RADIUS;
        FAR_SPAWN_CHANCE = SnowfallConfig.FAR_SPAWN_CHANCE;
    }

    public static boolean isSnowWeatherActive() {
        return isSnowWeatherActive;
    }

    public static Vec3 getPlayerVelocity() {
        return playerVelocity;
    }
}