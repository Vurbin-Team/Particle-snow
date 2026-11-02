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
    public int maxParticleCount = 32000;

    // Movement Settings
    public float playerFollowStrength = 0.015f;
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
    public float windStrength = 0.02f;
    public float windAngle = 0.0f;
    public float rotationSpeed = 0.02f;

    public boolean enableWeatherSound = true;
    public boolean coldBiomesOnly = false;
    public boolean disableLightning = true;

    // Texture Settings
    public boolean realisticSnow = false;

    // Effects (independent of presets)
    public boolean enableWindGusts = true;
    public float gustStrength = 1.0f;
    public float gustPeriodSeconds = 12.0f;

    public boolean enableSpeedStreaks = true;
    public float streakSpeedThreshold = 0.5f;
    public float streakDensity = 1.0f;
    public float streakLength = 1.0f;
    public float streakCenterClearance = 0.45f;

    public boolean enableFrostBreath = true;
    public float breathIntervalSeconds = 3.0f;
    public float breathSize = 1.0f;
    public float breathIntensity = 1.0f;
    public boolean breathFirstPerson = true;
}