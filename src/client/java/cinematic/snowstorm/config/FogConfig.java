package cinematic.snowstorm.config;

/**
 * Configuration class for fog effects
 * Modify these values to customize your fog
 */
public class FogConfig {

    // Enable/disable weather fog effects
    public static boolean ENABLE_RAIN_FOG = true;
    public static boolean ENABLE_THUNDER_FOG = true;

    // Rain fog settings (distance multiplier - lower = closer fog)
    public static float RAIN_FOG_START = 0.3f;  // Start distance (40% of render distance)
    public static float RAIN_FOG_END = 0.6f;    // End distance (80% of render distance)

    // Thunder fog settings (even closer fog for dramatic effect)
    public static float THUNDER_FOG_START = 0.2f;  // Start distance (30% of render distance)
    public static float THUNDER_FOG_END = 0.5f;    // End distance (70% of render distance)

    // Fog density/thickness (higher = thicker fog)
    public static float RAIN_FOG_DENSITY = 1.0f;
    public static float THUNDER_FOG_DENSITY = 1.3f;

    // Sky fog settings (applies to sky rendering)
    public static float RAIN_SKY_FOG_START = 0.3f;
    public static float RAIN_SKY_FOG_END = 0.6f;
    public static float THUNDER_SKY_FOG_START = 0.2f;
    public static float THUNDER_SKY_FOG_END = 0.5f;

    // Fog color tint (RGB values 0.0 - 1.0)
    // Cozy winter vibes - soft blue-white tones
    // Rain fog - gentle winter blue with soft white
    public static float RAIN_FOG_RED = 0.88f;
    public static float RAIN_FOG_GREEN = 0.92f;
    public static float RAIN_FOG_BLUE = 0.98f;

    // Thunder fog - deeper winter evening blue
    public static float THUNDER_FOG_RED = 0.75f;
    public static float THUNDER_FOG_GREEN = 0.82f;
    public static float THUNDER_FOG_BLUE = 0.92f;

    // Sky color tint (for cozy winter atmosphere)
    // Rain sky - soft cloudy winter white
    public static float RAIN_SKY_RED = 0.90f;
    public static float RAIN_SKY_GREEN = 0.93f;
    public static float RAIN_SKY_BLUE = 0.96f;

    // Thunder sky - moody winter storm blue
    public static float THUNDER_SKY_RED = 0.72f;
    public static float THUNDER_SKY_GREEN = 0.78f;
    public static float THUNDER_SKY_BLUE = 0.88f;
}