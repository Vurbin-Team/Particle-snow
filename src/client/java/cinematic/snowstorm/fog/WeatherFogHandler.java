package cinematic.snowstorm.fog;

import cinematic.snowstorm.config.FogConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.world.World;

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

    // Smooth transition speed
    private static final float TRANSITION_SPEED = 0.05f;

    /**
     * Updates fog values based on current weather
     * Call this every frame
     */
    public static void updateFog() {
        MinecraftClient client = MinecraftClient.getInstance();
        World world = client.world;

        if (world == null) {
            targetFogStart = 1.0f;
            targetFogEnd = 1.0f;
            targetSkyFogStart = 1.0f;
            targetSkyFogEnd = 1.0f;
            return;
        }

        // Check weather conditions
        boolean isRaining = world.isRaining();
        boolean isThundering = world.isThundering();

        // Set target fog values based on weather
        if (isThundering && FogConfig.ENABLE_THUNDER_FOG) {
            targetFogStart = FogConfig.THUNDER_FOG_START;
            targetFogEnd = FogConfig.THUNDER_FOG_END;
            targetSkyFogStart = FogConfig.THUNDER_SKY_FOG_START;
            targetSkyFogEnd = FogConfig.THUNDER_SKY_FOG_END;
        } else if (isRaining && FogConfig.ENABLE_RAIN_FOG) {
            targetFogStart = FogConfig.RAIN_FOG_START;
            targetFogEnd = FogConfig.RAIN_FOG_END;
            targetSkyFogStart = FogConfig.RAIN_SKY_FOG_START;
            targetSkyFogEnd = FogConfig.RAIN_SKY_FOG_END;
        } else {
            // Clear weather - no fog
            targetFogStart = 1.0f;
            targetFogEnd = 1.0f;
            targetSkyFogStart = 1.0f;
            targetSkyFogEnd = 1.0f;
        }

        // Smoothly transition to target values
        currentFogStart = lerp(currentFogStart, targetFogStart, TRANSITION_SPEED);
        currentFogEnd = lerp(currentFogEnd, targetFogEnd, TRANSITION_SPEED);
        currentSkyFogStart = lerp(currentSkyFogStart, targetSkyFogStart, TRANSITION_SPEED);
        currentSkyFogEnd = lerp(currentSkyFogEnd, targetSkyFogEnd, TRANSITION_SPEED);
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
     * Check if weather fog should be applied
     */
    public static boolean shouldApplyWeatherFog() {
        MinecraftClient client = MinecraftClient.getInstance();
        World world = client.world;

        if (world == null) return false;

        boolean isRaining = world.isRaining();
        boolean isThundering = world.isThundering();

        return (isRaining && FogConfig.ENABLE_RAIN_FOG) ||
                (isThundering && FogConfig.ENABLE_THUNDER_FOG);
    }

    /**
     * Get fog color multiplier based on weather
     * @param isSky true for sky fog, false for terrain fog
     */
    public static float[] getFogColor(boolean isSky) {
        MinecraftClient client = MinecraftClient.getInstance();
        World world = client.world;

        if (world == null) return new float[]{1.0f, 1.0f, 1.0f};

        if (world.isThundering() && FogConfig.ENABLE_THUNDER_FOG) {
            if (isSky) {
                return new float[]{
                        FogConfig.THUNDER_SKY_RED,
                        FogConfig.THUNDER_SKY_GREEN,
                        FogConfig.THUNDER_SKY_BLUE
                };
            } else {
                return new float[]{
                        FogConfig.THUNDER_FOG_RED,
                        FogConfig.THUNDER_FOG_GREEN,
                        FogConfig.THUNDER_FOG_BLUE
                };
            }
        } else if (world.isRaining() && FogConfig.ENABLE_RAIN_FOG) {
            if (isSky) {
                return new float[]{
                        FogConfig.RAIN_SKY_RED,
                        FogConfig.RAIN_SKY_GREEN,
                        FogConfig.RAIN_SKY_BLUE
                };
            } else {
                return new float[]{
                        FogConfig.RAIN_FOG_RED,
                        FogConfig.RAIN_FOG_GREEN,
                        FogConfig.RAIN_FOG_BLUE
                };
            }
        }

        return new float[]{1.0f, 1.0f, 1.0f};
    }
}