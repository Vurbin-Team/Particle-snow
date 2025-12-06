package cinematic.snowstorm.config;

/**
 * Configuration for snowfall particle system
 * Adjust these values to customize your snowfall effect
 */
public class SnowfallConfig {
    // weather sounds
    public static boolean ENABLE_WEATHER_SOUND = true;

    // ==== SPAWN SETTINGS ====

    // How high above player to spawn particles
    public static int SPAWN_HEIGHT = 40;

    // Horizontal radius around player (close particles)
    public static int SPAWN_RADIUS = 80;

    // Far distance spawning for cinematic depth
    public static int FAR_SPAWN_RADIUS = 120;
    public static float FAR_SPAWN_CHANCE = 0.3f;  // 30% spawn far away
    public static int PARTICLES_PER_TICK = 200;

    public static int GLOBAL_WIND_ANGLE_X = 10;

    // Ticks between spawn cycles (lower = more frequent)
    public static int SPAWN_INTERVAL = 3;


    // ==== MOVEMENT SETTINGS ====

    // How much particles follow player movement (0.0 - 1.0)
    // Higher = better follows fast movement
    public static float PLAYER_FOLLOW_STRENGTH = 0.015f;

    // Air resistance (0.95 = high resistance, 0.99 = low resistance)
    public static float AIR_DRAG = 0.98f;

    // How fast particles fall
    public static float FALL_SPEED_MIN = 0.08f;
    public static float FALL_SPEED_MAX = 0.10f;


    // ==== VISUAL SETTINGS ====

    // Particle size
    public static float SIZE_MIN = 0.18f;
    public static float SIZE_MAX = 0.25f;

    // Particle transparency
    public static float ALPHA_MIN = 0.30f;
    public static float ALPHA_MAX = 0.45f;


    // ==== MOVEMENT STYLE ====

    // Swaying amplitude (higher = more sway)
    public static float SWAY_AMOUNT_MIN = 0.018f;
    public static float SWAY_AMOUNT_MAX = 0.043f;

    // Swaying speed (higher = faster movement)
    public static float SWAY_SPEED = 0.4f;

    // Wind/drift strength (higher = more wind effect)
    public static float WIND_MIN = 0.5f;
    public static float WIND_MAX = 1.0f;

    // Rotation speed (visual spinning)
    public static float ROTATION_SPEED = 0.02f;


    // ==== PRESETS ====

    /**
     * Light gentle snowfall
     */
    public static void applyLightSnowPreset() {
        PARTICLES_PER_TICK = 70;
        SPAWN_HEIGHT = 35;
        SPAWN_RADIUS = 60;
        FAR_SPAWN_RADIUS = 90;
        FAR_SPAWN_CHANCE = 0.2f;
        FALL_SPEED_MIN = 0.06f;
        FALL_SPEED_MAX = 0.08f;
        SWAY_AMOUNT_MIN = 0.015f;
        SWAY_AMOUNT_MAX = 0.030f;
        WIND_MIN = 0.3f;
        WIND_MAX = 0.6f;
    }

    /**
     * Heavy blizzard
     */
    public static void applyBlizzardPreset() {
        PARTICLES_PER_TICK = 200;
        SPAWN_HEIGHT = 45;
        SPAWN_RADIUS = 100;
        FAR_SPAWN_RADIUS = 150;
        FAR_SPAWN_CHANCE = 0.4f;
        FALL_SPEED_MIN = 0.12f;
        FALL_SPEED_MAX = 0.16f;
        SWAY_AMOUNT_MIN = 0.030f;
        SWAY_AMOUNT_MAX = 0.060f;
        WIND_MIN = 1.2f;
        WIND_MAX = 1.8f;
        AIR_DRAG = 0.96f;
    }

    /**
     * Magical slow-motion snow
     */
    public static void applyMagicalPreset() {
        PARTICLES_PER_TICK = 120;
        SPAWN_HEIGHT = 40;
        SPAWN_RADIUS = 70;
        FAR_SPAWN_RADIUS = 110;
        FAR_SPAWN_CHANCE = 0.35f;
        FALL_SPEED_MIN = 0.04f;
        FALL_SPEED_MAX = 0.06f;
        SWAY_AMOUNT_MIN = 0.025f;
        SWAY_AMOUNT_MAX = 0.045f;
        SIZE_MIN = 0.30f;
        SIZE_MAX = 0.50f;
        ALPHA_MIN = 0.80f;
        ALPHA_MAX = 0.95f;
        ROTATION_SPEED = 0.03f;
    }

    /**
     * Cinematic wide-area snow (snow everywhere!)
     */
    public static void applyCinematicPreset() {
        PARTICLES_PER_TICK = 180;
        SPAWN_HEIGHT = 45;
        SPAWN_RADIUS = 90;
        FAR_SPAWN_RADIUS = 140;
        FAR_SPAWN_CHANCE = 0.45f;  // Almost half spawn far away
        FALL_SPEED_MIN = 0.08f;
        FALL_SPEED_MAX = 0.12f;
        SWAY_AMOUNT_MIN = 0.020f;
        SWAY_AMOUNT_MAX = 0.040f;
        SIZE_MIN = 0.28f;
        SIZE_MAX = 0.45f;
        WIND_MIN = 0.6f;
        WIND_MAX = 1.2f;
    }
}