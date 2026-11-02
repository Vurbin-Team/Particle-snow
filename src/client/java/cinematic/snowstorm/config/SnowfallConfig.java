package cinematic.snowstorm.config;

/**
 * Configuration for snowfall particle system
 * Adjust these values to customize your snowfall effect
 */
public class SnowfallConfig {
    // weather sounds
    public static boolean ENABLE_WEATHER_SOUND = true;
    public static int MAX_PARTICLE_COUNT = 32000;
    // Fixed capacity of the engine's particle queue (see IncreaseParticleLimitMixin).
    // MAX_PARTICLE_COUNT is enforced by SnowSpawnManager and may change at runtime up to this value.
    public static final int PARTICLE_QUEUE_CAPACITY = 100_000;

    // ==== WEATHER ====

    // false: snow wherever vanilla has rain or snow (never in deserts, Nether, End).
    // true: only in cold biomes. Rain in warm biomes is then not drawn at all,
    // because the mod disables the vanilla weather renderer.
    public static boolean COLD_BIOMES_ONLY = false;

    // true: lightning bolts are not drawn (the storm has no flashes).
    public static boolean DISABLE_LIGHTNING = true;

    // ==== TEXTURE SETTINGS ====

    // Use soft, photo-realistic snow textures instead of the stylized pixel-art ones
    public static boolean REALISTIC_SNOW = false;

    // ==== EFFECTS ====
    // Independent of the presets: presets never touch these values.

    // Wind gusts: strength is a multiplier (1.0 = gusts add up to 0.03 blocks/tick), period in seconds
    public static boolean ENABLE_WIND_GUSTS = true;
    public static float GUST_STRENGTH = 1.0f;
    public static float GUST_PERIOD_SECONDS = 12.0f;

    // Speed streaks: threshold in blocks per tick (0.5 = 10 blocks/s), density and length are multipliers
    public static boolean ENABLE_SPEED_STREAKS = true;
    public static float STREAK_SPEED_THRESHOLD = 0.5f;
    public static float STREAK_DENSITY = 1.0f;
    public static float STREAK_LENGTH = 1.0f;
    // Share of the view (0.0 - 0.9) around the screen center that stays free of streaks
    public static float STREAK_CENTER_CLEARANCE = 0.45f;

    // Frost breath: interval in seconds, size scales the puffs (1.0 = default size), intensity is their opacity
    public static boolean ENABLE_FROST_BREATH = true;
    public static float BREATH_INTERVAL_SECONDS = 3.0f;
    public static float BREATH_SIZE = 1.0f;
    public static float BREATH_INTENSITY = 1.0f;
    public static boolean BREATH_FIRST_PERSON = true;

    // ==== SPAWN SETTINGS ====

    // How high above player to spawn particles
    public static int SPAWN_HEIGHT = 40;

    // Horizontal radius around player (close particles)
    public static int SPAWN_RADIUS = 80;

    // Far distance spawning for cinematic depth
    public static int FAR_SPAWN_RADIUS = 120;
    public static float FAR_SPAWN_CHANCE = 0.3f;  // 30% spawn far away
    public static int PARTICLES_PER_TICK = 200;

    // Ticks between spawn cycles (lower = more frequent)
    public static int SPAWN_INTERVAL = 3;


    // ==== MOVEMENT SETTINGS ====

    // Additional horizontal particle movement as a fraction of player velocity (0.0 - 1.0)
    public static float PLAYER_FOLLOW_STRENGTH = 0.015f;

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

    // Horizontal wind velocity in blocks per tick; applied immediately when particles spawn.
    public static float WIND_STRENGTH = 0.02f;
    // Direction of particle movement in degrees: 0 = +X, 90 = +Z.
    public static float WIND_ANGLE = 0.0f;

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
        WIND_STRENGTH = 0.01f;
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
        WIND_STRENGTH = 0.06f;
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
        WIND_STRENGTH = 0.005f;
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
        WIND_STRENGTH = 0.02f;
    }
}