package cinematic.snowstorm.utils;

import cinematic.snowstorm.particle.ParticleTypes;
import cinematic.snowstorm.config.SnowfallConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec3d;

import java.lang.reflect.Field;

public class SnowSpawnManager {
    // Spawn area configuration
    private static int SPAWN_HEIGHT_ABOVE = SnowfallConfig.SPAWN_HEIGHT;
    private static int SPAWN_RADIUS_HORIZONTAL = SnowfallConfig.SPAWN_RADIUS;
    private static int SPAWN_COUNT_PER_TICK = SnowfallConfig.PARTICLES_PER_TICK;
    private static int TICKS_BETWEEN_SPAWN = SnowfallConfig.SPAWN_INTERVAL;

    // Far distance spawning for atmospheric effect
    private static int FAR_SPAWN_RADIUS = SnowfallConfig.FAR_SPAWN_RADIUS;
    private static float FAR_SPAWN_CHANCE = SnowfallConfig.FAR_SPAWN_CHANCE;

    // Player motion tracking
    private static Vec3d lastPlayerPos = Vec3d.ZERO;
    private static Vec3d playerVelocity = Vec3d.ZERO;
    private static final float VELOCITY_SMOOTHING = 0.3f;

    private static int tickCounter = 0;
    private static boolean isSnowWeatherActive = false;

    // Cached wind compensation (recalculated only on config change)
    private static float cachedDriftX = 0f;
    private static float cachedDriftZ = 0f;

    public static void init() {
        ClientTickEvents.END_WORLD_TICK.register(world -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player == null) return;
            if (!(world instanceof ClientWorld)) return;

            // Track player velocity for prediction
            Vec3d currentPos = mc.player.getPos();
            Vec3d currentVelocity = currentPos.subtract(lastPlayerPos);

            // Smooth velocity to avoid jitter
            playerVelocity = playerVelocity.multiply(1.0 - VELOCITY_SMOOTHING)
                    .add(currentVelocity.multiply(VELOCITY_SMOOTHING));

            lastPlayerPos = currentPos;

            // Check weather conditions
            boolean shouldSnow = world.isRaining() || world.isThundering();
            isSnowWeatherActive = shouldSnow;

            if (shouldSnow) {
                tickCounter++;
                if (tickCounter < TICKS_BETWEEN_SPAWN) return;
                tickCounter = 0;

                double px = mc.player.getX() + playerVelocity.x * 5; // Predict ahead
                double py = mc.player.getY();
                double pz = mc.player.getZ() + playerVelocity.z * 5;

                // Spawn particles in a circle around and above the player
                for (int i = 0; i < SPAWN_COUNT_PER_TICK; i++) {
                    // Decide if this particle should spawn far away for atmospheric effect
                    boolean spawnFar = world.random.nextFloat() < FAR_SPAWN_CHANCE;
                    int maxRadius = spawnFar ? FAR_SPAWN_RADIUS : SPAWN_RADIUS_HORIZONTAL;

                    // Random position in circular area above player
                    double angle = world.random.nextDouble() * Math.PI * 2;
                    double distance = Math.sqrt(world.random.nextDouble()) * maxRadius;

                    // APPLY WIND COMPENSATION: Spawn upwind so particles land in target area
                    double dx = px + Math.cos(angle) * distance - cachedDriftX;
                    double dz = pz + Math.sin(angle) * distance - cachedDriftZ;

                    // Randomize height for more natural distribution
                    int heightVariation = spawnFar ? 25 : 15;
                    double dy = py + SPAWN_HEIGHT_ABOVE + world.random.nextInt(heightVariation);

                    // Add slight initial velocity matching player movement
                    double vx = playerVelocity.x * 0.5;
                    double vz = playerVelocity.z * 0.5;

                    // Use alwaysSpawn flag to force rendering at distance
                    world.addImportantParticle(
                            ParticleTypes.MY_SNOWFLAKE,
                            true,  // alwaysSpawn - bypasses distance check!
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
        lastPlayerPos = Vec3d.ZERO;
        playerVelocity = Vec3d.ZERO;

        // Clear all existing particles
        clearAllParticles();
    }

    /**
     * Remove all particles from the world
     */
    private static void clearAllParticles() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc != null && mc.particleManager != null) {
            try {
                Field particlesField = mc.particleManager.getClass().getDeclaredField("particles");
                particlesField.setAccessible(true);

                Object particlesMap = particlesField.get(mc.particleManager);
                if (particlesMap instanceof java.util.Map) {
                    ((java.util.Map<?, ?>) particlesMap).clear();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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

        // Recalculate wind compensation when config changes
        calculateWindCompensation();
    }

    /**
     * Calculate wind drift compensation by simulating particle physics
     * This is cached and only recalculated when config changes
     */
    private static void calculateWindCompensation() {
        float avgFallSpeed = (SnowfallConfig.FALL_SPEED_MIN + SnowfallConfig.FALL_SPEED_MAX) / 2f;
        float avgWindStrength = (SnowfallConfig.WIND_MIN + SnowfallConfig.WIND_MAX) / 2f;

        // Simulate particle falling from spawn height to ground
        float simVelX = 0f;
        float simVelZ = 0f;
        float simPosX = 0f;
        float simPosZ = 0f;
        float simHeight = SPAWN_HEIGHT_ABOVE;

        // Wind force per tick (matches MySnowflakeParticle.tick())
        float windForceX = (float)Math.cos(SnowfallConfig.GLOBAL_WIND_ANGLE_X) * avgWindStrength * 0.002f;
        float windForceZ = (float)Math.sin(SnowfallConfig.GLOBAL_WIND_ANGLE_X) * avgWindStrength * 0.002f;

        // Simulate fall until hitting ground
        while (simHeight > 0) {
            // Apply wind force to velocity
            simVelX += windForceX;
            simVelZ += windForceZ;

            // Apply drag
            simVelX *= SnowfallConfig.AIR_DRAG;
            simVelZ *= SnowfallConfig.AIR_DRAG;

            // Move particle
            simPosX += simVelX;
            simPosZ += simVelZ;
            simHeight -= avgFallSpeed;
        }

        // Cache the result
        cachedDriftX = simPosX;
        cachedDriftZ = simPosZ;
    }

    public static boolean isSnowWeatherActive() {
        return isSnowWeatherActive;
    }

    public static Vec3d getPlayerVelocity() {
        return playerVelocity;
    }
}