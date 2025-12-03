package cinematic.snowstorm.utils;

import cinematic.snowstorm.particle.ParticleTypes;
import cinematic.snowstorm.config.SnowfallConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec3d;

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
    private static Vec3d lastPlayerPos = Vec3d.ZERO;
    private static Vec3d playerVelocity = Vec3d.ZERO;
    private static final float VELOCITY_SMOOTHING = 0.3f;

    private static int tickCounter = 0;
    private static boolean isSnowWeatherActive = false;

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

                // Get player position with velocity prediction
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

                    double dx = px + Math.cos(angle) * distance;
                    double dz = pz + Math.sin(angle) * distance;

                    // Randomize height for more natural distribution
                    // Far particles spawn even higher for depth
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

    public static Vec3d getPlayerVelocity() {
        return playerVelocity;
    }
}