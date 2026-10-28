package cinematic.snowstorm.utils;

import cinematic.snowstorm.config.SnowfallConfig;
import cinematic.snowstorm.fog.WeatherFogHandler;
import cinematic.snowstorm.particle.MySnowflakeParticle;
import cinematic.snowstorm.particle.ParticleTypes;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

public class SnowSpawnManager {
    private static int SPAWN_HEIGHT_ABOVE = SnowfallConfig.SPAWN_HEIGHT;
    private static int SPAWN_RADIUS_HORIZONTAL = SnowfallConfig.SPAWN_RADIUS;
    private static int SPAWN_COUNT_PER_TICK = SnowfallConfig.PARTICLES_PER_TICK;
    private static int TICKS_BETWEEN_SPAWN = SnowfallConfig.SPAWN_INTERVAL;
    private static int FAR_SPAWN_RADIUS = SnowfallConfig.FAR_SPAWN_RADIUS;
    private static float FAR_SPAWN_CHANCE = SnowfallConfig.FAR_SPAWN_CHANCE;

    // Player motion tracking
    private static Vec3 lastPlayerPos = Vec3.ZERO;
    private static Vec3 playerVelocity = Vec3.ZERO;
    private static boolean hasLastPlayerPos;
    private static Object lastLevel;
    private static final float VELOCITY_SMOOTHING = 0.3f;
    // Tracked speed is clamped; a move farther than this in one tick is a teleport, not motion.
    private static final double MAX_TRACKED_SPEED = 8.0;
    private static final double TELEPORT_DIST_SQ = 16.0 * 16.0;

    // Spawn budget is spread over every tick instead of one burst per SPAWN_INTERVAL ticks.
    private static float spawnAccumulator = 0f;
    private static boolean isSnowWeatherActive = false;
    private static boolean wasSnowing = false;

    // Pre-fill: when snow starts (or after a teleport/join) fill the whole fall column at once,
    // otherwise the first flakes need ~20 s to fall from the spawn plane to the player.
    private static final int PREFILL_TICKS = 40;
    private static final double PREFILL_BELOW = 16.0;
    private static int prefillTicksLeft = 0;

    // Live snow particles (MAX_PARTICLE_COUNT is enforced here, not by queue eviction).
    private static int liveCount = 0;
    private static int idleTicks = 0;
    private static final int ALL_DEAD_AFTER_TICKS = 700; // lifetime is 600

    // Distance culling state, read by MySnowflakeParticle every few ticks.
    // MAX_VALUE = "don't cull" until the first player tick has filled it in.
    private static double cullX, cullY, cullZ;
    private static double cullRadiusSq = Double.MAX_VALUE;
    private static double cullTop = Double.MAX_VALUE;
    private static final double CULL_BOTTOM = -32.0;

    public static void init() {
        loadConfigValues();

        ClientTickEvents.END_LEVEL_TICK.register(world -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) {
                lastPlayerPos = Vec3.ZERO;
                playerVelocity = Vec3.ZERO;
                hasLastPlayerPos = false;
                lastLevel = null;
                isSnowWeatherActive = false;
                wasSnowing = false;
                spawnAccumulator = 0f;
                prefillTicksLeft = 0;
                liveCount = 0;
                cullRadiusSq = Double.MAX_VALUE;
                cullTop = Double.MAX_VALUE;
                return;
            }

            Vec3 currentPos = mc.player.position();
            boolean teleported = false;
            if (!hasLastPlayerPos || world != lastLevel) {
                // Join or dimension change: the engine dropped old particles without calling remove().
                playerVelocity = Vec3.ZERO;
                hasLastPlayerPos = true;
                lastLevel = world;
                liveCount = 0;
                teleported = true;
            } else {
                Vec3 currentVelocity = currentPos.subtract(lastPlayerPos);
                if (currentVelocity.lengthSqr() > TELEPORT_DIST_SQ) {
                    playerVelocity = Vec3.ZERO;
                    teleported = true;
                } else {
                    playerVelocity = playerVelocity.scale(1.0 - VELOCITY_SMOOTHING)
                            .add(currentVelocity.scale(VELOCITY_SMOOTHING));
                    double speed = playerVelocity.length();
                    if (speed > MAX_TRACKED_SPEED) {
                        playerVelocity = playerVelocity.scale(MAX_TRACKED_SPEED / speed);
                    }
                }
            }
            lastPlayerPos = currentPos;

            // ---- per-tick invariants (were recomputed for every particle before) ----
            // Radii never exceed the real fog end: anything farther is invisible anyway.
            float fogEnd = Math.max(16.0f, WeatherFogHandler.getEffectiveFogEnd());
            int nearRadius = (int) Math.min(SPAWN_RADIUS_HORIZONTAL, fogEnd);
            int farRadius = (int) Math.min(FAR_SPAWN_RADIUS, fogEnd);

            // Smaller area -> proportionally fewer flakes, so visible density stays the same.
            double farChance = FAR_SPAWN_CHANCE;
            double configuredArea = (1 - farChance) * (double) SPAWN_RADIUS_HORIZONTAL * SPAWN_RADIUS_HORIZONTAL
                    + farChance * (double) FAR_SPAWN_RADIUS * FAR_SPAWN_RADIUS;
            double effectiveArea = (1 - farChance) * (double) nearRadius * nearRadius
                    + farChance * (double) farRadius * farRadius;
            double areaScale = configuredArea > 0 ? Math.min(1.0, effectiveArea / configuredArea) : 1.0;

            float averageFallSpeed = Math.max(
                    0.001f,
                    (Math.max(0.0f, SnowfallConfig.FALL_SPEED_MIN)
                            + Math.max(0.0f, SnowfallConfig.FALL_SPEED_MAX)) * 0.5f);
            int estimatedFallTicks = (int) Math.ceil(SPAWN_HEIGHT_ABOVE / averageFallSpeed);
            Vec3 windDrift = MySnowflakeParticle.getExpectedWindDrift(estimatedFallTicks);
            double windAngle = Math.toRadians(SnowfallConfig.WIND_ANGLE);
            double windStrength = Math.max(0.0, SnowfallConfig.WIND_STRENGTH);
            double directionLength = Math.sqrt(windStrength * windStrength
                    + averageFallSpeed * averageFallSpeed);
            double windX = Math.cos(windAngle);
            double windZ = Math.sin(windAngle);
            double crosswindX = -windZ;
            double crosswindZ = windX;
            double planeX = averageFallSpeed * windX / directionLength;
            double planeY = windStrength / directionLength;
            double planeZ = averageFallSpeed * windZ / directionLength;

            // ---- cull volume: spawn area + wind offset + look-ahead + margin ----
            double speedXZ = Math.hypot(playerVelocity.x, playerVelocity.z);
            double cullRadius = farRadius + Math.hypot(windDrift.x, windDrift.z) + speedXZ * 5 + 16;
            cullX = currentPos.x;
            cullY = currentPos.y;
            cullZ = currentPos.z;
            cullRadiusSq = cullRadius * cullRadius;
            // The spawn disk is tilted along the wind, so its top edge is above SPAWN_HEIGHT.
            cullTop = SPAWN_HEIGHT_ABOVE + planeY * farRadius + 16;

            boolean shouldSnow = world.isRaining() || world.isThundering();
            isSnowWeatherActive = shouldSnow;
            if (!shouldSnow) {
                wasSnowing = false;
                spawnAccumulator = 0f;
                prefillTicksLeft = 0;
                if (++idleTicks > ALL_DEAD_AFTER_TICKS) {
                    liveCount = 0; // every flake is dead by now; drops any drift in the counter
                }
                return;
            }
            idleTicks = 0;
            if (!wasSnowing || teleported) {
                prefillTicksLeft = PREFILL_TICKS;
            }
            wasSnowing = true;

            double perTickRate = (double) SPAWN_COUNT_PER_TICK / Math.max(1, TICKS_BETWEEN_SPAWN) * areaScale;
            spawnAccumulator += (float) perTickRate;
            int regularCount = (int) spawnAccumulator;
            spawnAccumulator -= regularCount;

            int prefillCount = 0;
            if (prefillTicksLeft > 0) {
                prefillTicksLeft--;
                double steadyState = perTickRate * (SPAWN_HEIGHT_ABOVE + PREFILL_BELOW) / averageFallSpeed;
                prefillCount = (int) Math.ceil(steadyState / PREFILL_TICKS);
            }

            double px = currentPos.x + playerVelocity.x * 5;
            double py = currentPos.y;
            double pz = currentPos.z + playerVelocity.z * 5;
            var snowType = ParticleTypes.getActiveSnowType();
            int maxCount = SnowfallConfig.MAX_PARTICLE_COUNT;
            int total = regularCount + prefillCount;

            for (int i = 0; i < total; i++) {
                if (liveCount >= maxCount) break;

                // f = fraction of the spawn height already fallen. 0 for regular flakes (spawn plane);
                // prefill flakes start somewhere down the column, with the wind drift they would
                // already have accumulated.
                double f = i < regularCount
                        ? 0.0
                        : world.getRandom().nextDouble() * (SPAWN_HEIGHT_ABOVE + PREFILL_BELOW) / SPAWN_HEIGHT_ABOVE;

                boolean spawnFar = world.getRandom().nextFloat() < FAR_SPAWN_CHANCE;
                int maxRadius = spawnFar ? farRadius : nearRadius;

                double diskAngle = world.getRandom().nextDouble() * Math.PI * 2;
                double diskRadius = Math.sqrt(world.getRandom().nextDouble()) * maxRadius;
                double diskX = Math.cos(diskAngle) * diskRadius;
                double diskY = Math.sin(diskAngle) * diskRadius;

                double dx = px - windDrift.x * (1 - f) + diskX * crosswindX + diskY * planeX;
                double dy = py + SPAWN_HEIGHT_ABOVE * (1 - f) + diskY * planeY;
                double dz = pz - windDrift.z * (1 - f) + diskX * crosswindZ + diskY * planeZ;

                // Skip points in unloaded chunks and points inside terrain / under roofs
                // (caves, buildings): such a flake would die on its first tick anyway.
                int bx = (int) Math.floor(dx);
                int bz = (int) Math.floor(dz);
                if (!world.hasChunkAt(new BlockPos(bx, (int) Math.floor(dy), bz))) continue;
                if (dy <= world.getHeight(Heightmap.Types.MOTION_BLOCKING, bx, bz)) continue;

                world.addParticle(snowType, true, true, dx, dy, dz, 0, 0, 0);
            }
        });
    }

    /**
     * True when a flake is outside the volume that can still be visible/relevant
     * (left behind while flying, far above/below the player after altitude change).
     */
    public static boolean shouldCull(double x, double y, double z) {
        double dx = x - cullX;
        double dz = z - cullZ;
        double dy = y - cullY;
        return dx * dx + dz * dz > cullRadiusSq || dy > cullTop || dy < CULL_BOTTOM;
    }

    public static void onParticleCreated() {
        liveCount++;
    }

    public static void onParticleRemoved() {
        if (liveCount > 0) liveCount--;
    }

    public static void reload() {
        // Config saved from the menu: just re-read values. Player/level tracking must stay intact,
        // otherwise the next tick would look like a join (counter reset + prefill burst).
        loadConfigValues();
        spawnAccumulator = 0f;
    }

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
