package cinematic.snowstorm.config;

/**
 * Serializable configuration data for JSON storage
 */
public class SnowfallConfigData {
    // Spawn Settings
    public int spawnHeight = 40;
    public int spawnRadius = 80;
    public int farSpawnRadius = 120;
    public float farSpawnChance = 0.3f;
    public int particlesPerTick = 200;
    public int spawnInterval = 3;

    // Movement Settings
    public float playerFollowStrength = 0.015f;
    public float airDrag = 0.98f;
    public float fallSpeedMin = 0.08f;
    public float fallSpeedMax = 0.10f;

    // Visual Settings
    public float sizeMin = 0.18f;
    public float sizeMax = 0.25f;
    public float alphaMin = 0.30f;
    public float alphaMax = 0.45f;

    // Movement Style
    public float swayAmountMin = 0.018f;
    public float swayAmountMax = 0.043f;
    public float swaySpeed = 0.4f;
    public float windMin = 0.5f;
    public float windMax = 1.0f;
    public float rotationSpeed = 0.02f;

    public boolean enableWeatherSound = true;
}