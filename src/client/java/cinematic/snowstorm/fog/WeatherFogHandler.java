package cinematic.snowstorm.fog;

import cinematic.snowstorm.config.FogConfig;
import cinematic.snowstorm.utils.WeatherState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * Handler class for weather-based fog effects
 */
public class WeatherFogHandler {

    private static float currentFogStart = 1.0f;
    private static float currentFogEnd = 1.0f;
    private static float currentSkyFogStart = 1.0f;
    private static float currentSkyFogEnd = 1.0f;
    private static float targetFogStart = 1.0f;
    private static float targetFogEnd = 1.0f;
    private static float targetSkyFogStart = 1.0f;
    private static float targetSkyFogEnd = 1.0f;
    private static final float[] currentFogColor = {1.0f, 1.0f, 1.0f};
    private static final float[] targetFogColor = {1.0f, 1.0f, 1.0f};
    // True while any multiplier differs from 1, i.e. also while the fog fades out after the weather ended.
    private static boolean fogActive;

    // Real fog end in blocks (after weather scaling); snow spawn/cull radii are clamped to it.
    private static float effectiveFogEnd = Float.MAX_VALUE;

    public static float getEffectiveFogEnd() {
        return effectiveFogEnd;
    }

    public static void setEffectiveFogEnd(float value) {
        effectiveFogEnd = value;
    }

    // Exponential approach rate per second (equals the old 0.05 per frame at 60 FPS).
    // setupFog can run several times per frame, so the step is based on real time, not on call count.
    private static final float TRANSITION_RATE = 3.08f;
    private static long lastUpdateNanos = 0L;

    /**
     * Updates fog values based on current weather
     * Call this every frame
     */
    public static void updateFog() {
        Minecraft client = Minecraft.getInstance();
        Level world = client.level;
        LocalPlayer player = client.player;

        if (world == null || player == null) {
            // No world: drop the fog at once so it does not fade out over the next world.
            targetFogStart = currentFogStart = 1.0f;
            targetFogEnd = currentFogEnd = 1.0f;
            targetSkyFogStart = currentSkyFogStart = 1.0f;
            targetSkyFogEnd = currentSkyFogEnd = 1.0f;
            for (int i = 0; i < 3; i++) targetFogColor[i] = currentFogColor[i] = 1.0f;
            fogActive = false;
            return;
        }

        // The fog belongs to snow that reaches the player: right biome and open sky (not in caves).
        BlockPos pos = player.blockPosition();
        boolean snowing = WeatherState.isSnowing(world, pos) && world.canSeeSky(pos);
        boolean thunder = snowing && world.isThundering() && FogConfig.ENABLE_THUNDER_FOG;
        boolean rain = snowing && FogConfig.ENABLE_RAIN_FOG;

        // Set target fog values based on weather
        if (thunder) {
            targetFogStart = FogConfig.THUNDER_FOG_START;
            targetFogEnd = FogConfig.THUNDER_FOG_END;
            targetSkyFogStart = FogConfig.THUNDER_SKY_FOG_START;
            targetSkyFogEnd = FogConfig.THUNDER_SKY_FOG_END;
            setTargetColor(FogConfig.THUNDER_FOG_RED, FogConfig.THUNDER_FOG_GREEN, FogConfig.THUNDER_FOG_BLUE);
        } else if (rain) {
            targetFogStart = FogConfig.RAIN_FOG_START;
            targetFogEnd = FogConfig.RAIN_FOG_END;
            targetSkyFogStart = FogConfig.RAIN_SKY_FOG_START;
            targetSkyFogEnd = FogConfig.RAIN_SKY_FOG_END;
            setTargetColor(FogConfig.RAIN_FOG_RED, FogConfig.RAIN_FOG_GREEN, FogConfig.RAIN_FOG_BLUE);
        } else {
            // No snow reaches the player - no fog
            targetFogStart = 1.0f;
            targetFogEnd = 1.0f;
            targetSkyFogStart = 1.0f;
            targetSkyFogEnd = 1.0f;
            setTargetColor(1.0f, 1.0f, 1.0f);
        }

        // Smoothly transition to target values
        long now = System.nanoTime();
        float dt = lastUpdateNanos == 0L ? 0.016f : Math.min(0.25f, (now - lastUpdateNanos) / 1.0E9f);
        lastUpdateNanos = now;
        float step = 1.0f - (float) Math.exp(-TRANSITION_RATE * dt);
        currentFogStart = approach(currentFogStart, targetFogStart, step);
        currentFogEnd = approach(currentFogEnd, targetFogEnd, step);
        currentSkyFogStart = approach(currentSkyFogStart, targetSkyFogStart, step);
        currentSkyFogEnd = approach(currentSkyFogEnd, targetSkyFogEnd, step);
        for (int i = 0; i < 3; i++) {
            currentFogColor[i] = approach(currentFogColor[i], targetFogColor[i], step);
        }

        fogActive = currentFogStart != 1.0f || currentFogEnd != 1.0f || currentSkyFogEnd != 1.0f
                || currentFogColor[0] != 1.0f || currentFogColor[1] != 1.0f || currentFogColor[2] != 1.0f;
    }

    private static void setTargetColor(float r, float g, float b) {
        targetFogColor[0] = r;
        targetFogColor[1] = g;
        targetFogColor[2] = b;
    }

    /** Moves toward the target and snaps onto it when close, so "no fog" ends at exactly 1.0. */
    private static float approach(float current, float target, float step) {
        float value = lerp(current, target, step);
        return Math.abs(value - target) < 0.002f ? target : value;
    }

    /**
     * Linear interpolation for smooth transitions
     */
    private static float lerp(float start, float end, float t) {
        return start + (end - start) * t;
    }

    /**
     * Get current fog start distance
     */
    public static float getFogStart() {
        return currentFogStart;
    }

    /**
     * Get current fog end distance
     */
    public static float getFogEnd() {
        return currentFogEnd;
    }

    /**
     * Get current sky fog start distance
     */
    public static float getSkyFogStart() {
        return currentSkyFogStart;
    }

    /**
     * Get current sky fog end distance
     */
    public static float getSkyFogEnd() {
        return currentSkyFogEnd;
    }

    /**
     * Check if weather fog should be applied (also while it is still fading out)
     */
    public static boolean shouldApplyWeatherFog() {
        return fogActive;
    }

    /**
     * Get fog color multiplier based on weather
     * @param isSky true for sky fog, false for terrain fog (smoothed, fades with the fog distance)
     */
    public static float[] getFogColor(boolean isSky) {
        if (!isSky) return currentFogColor;

        Minecraft client = Minecraft.getInstance();
        Level world = client.level;

        if (world == null) return new float[]{1.0f, 1.0f, 1.0f};

        if (world.isThundering() && FogConfig.ENABLE_THUNDER_FOG) {
            return new float[]{
                    FogConfig.THUNDER_SKY_RED,
                    FogConfig.THUNDER_SKY_GREEN,
                    FogConfig.THUNDER_SKY_BLUE
            };
        } else if (world.isRaining() && FogConfig.ENABLE_RAIN_FOG) {
            return new float[]{
                    FogConfig.RAIN_SKY_RED,
                    FogConfig.RAIN_SKY_GREEN,
                    FogConfig.RAIN_SKY_BLUE
            };
        }

        return new float[]{1.0f, 1.0f, 1.0f};
    }
}
