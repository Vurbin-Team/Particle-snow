package cinematic.snowstorm.config;

import cinematic.snowstorm.utils.SnowSpawnManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Manages loading, saving, and applying snowfall configuration
 */
public class SnowfallConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("snowstorm.json");

    private static SnowfallConfigData configData = new SnowfallConfigData();

    /**
     * Load configuration from file, or create default if not exists
     */
    public static void load() {
        if (Files.exists(CONFIG_PATH)) {
            try {
                String json = Files.readString(CONFIG_PATH);
                configData = GSON.fromJson(json, SnowfallConfigData.class);
                applyToRuntime();
                System.out.println("[Snowstorm] Config loaded from " + CONFIG_PATH);
            } catch (IOException e) {
                System.err.println("[Snowstorm] Failed to load config: " + e.getMessage());
                configData = new SnowfallConfigData();
                save();
            }
        } else {
            // First time - save defaults
            configData = new SnowfallConfigData();
            save();
            System.out.println("[Snowstorm] Created default config at " + CONFIG_PATH);
        }
    }

    /**
     * Save current configuration to file
     */
    public static void save() {
        try {
            // Capture current runtime values
            captureFromRuntime();
            String json = GSON.toJson(configData);
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.writeString(CONFIG_PATH, json);
            System.out.println("[Snowstorm] Config saved");
        } catch (IOException e) {
            System.err.println("[Snowstorm] Failed to save config: " + e.getMessage());
        }
    }

    /**
     * Apply loaded config data to runtime static fields
     */
    private static void applyToRuntime() {
        SnowfallConfig.SPAWN_HEIGHT = configData.spawnHeight;
        SnowfallConfig.SPAWN_RADIUS = configData.spawnRadius;
        SnowfallConfig.FAR_SPAWN_RADIUS = configData.farSpawnRadius;
        SnowfallConfig.FAR_SPAWN_CHANCE = configData.farSpawnChance;
        SnowfallConfig.PARTICLES_PER_TICK = configData.particlesPerTick;
        SnowfallConfig.SPAWN_INTERVAL = configData.spawnInterval;
        SnowfallConfig.PLAYER_FOLLOW_STRENGTH = configData.playerFollowStrength;
        SnowfallConfig.FALL_SPEED_MIN = configData.fallSpeedMin;
        SnowfallConfig.FALL_SPEED_MAX = configData.fallSpeedMax;
        SnowfallConfig.SIZE_MIN = configData.sizeMin;
        SnowfallConfig.SIZE_MAX = configData.sizeMax;
        SnowfallConfig.ALPHA_MIN = configData.alphaMin;
        SnowfallConfig.ALPHA_MAX = configData.alphaMax;
        SnowfallConfig.SWAY_AMOUNT_MIN = configData.swayAmountMin;
        SnowfallConfig.SWAY_AMOUNT_MAX = configData.swayAmountMax;
        SnowfallConfig.SWAY_SPEED = configData.swaySpeed;
        SnowfallConfig.WIND_STRENGTH = Math.max(0.0f, configData.windStrength);
        SnowfallConfig.WIND_ANGLE = normalizeAngle(configData.windAngle);
        SnowfallConfig.ROTATION_SPEED = configData.rotationSpeed;
        SnowfallConfig.ENABLE_WEATHER_SOUND = configData.enableWeatherSound;
        SnowfallConfig.MAX_PARTICLE_COUNT = configData.maxParticleCount;
        SnowfallConfig.REALISTIC_SNOW = configData.realisticSnow;
    }

    /**
     * Capture current runtime values to config data
     */
    public static void captureFromRuntime() {
        configData.spawnHeight = SnowfallConfig.SPAWN_HEIGHT;
        configData.spawnRadius = SnowfallConfig.SPAWN_RADIUS;
        configData.farSpawnRadius = SnowfallConfig.FAR_SPAWN_RADIUS;
        configData.farSpawnChance = SnowfallConfig.FAR_SPAWN_CHANCE;
        configData.particlesPerTick = SnowfallConfig.PARTICLES_PER_TICK;
        configData.spawnInterval = SnowfallConfig.SPAWN_INTERVAL;
        configData.playerFollowStrength = SnowfallConfig.PLAYER_FOLLOW_STRENGTH;
        configData.fallSpeedMin = SnowfallConfig.FALL_SPEED_MIN;
        configData.fallSpeedMax = SnowfallConfig.FALL_SPEED_MAX;
        configData.sizeMin = SnowfallConfig.SIZE_MIN;
        configData.sizeMax = SnowfallConfig.SIZE_MAX;
        configData.alphaMin = SnowfallConfig.ALPHA_MIN;
        configData.alphaMax = SnowfallConfig.ALPHA_MAX;
        configData.swayAmountMin = SnowfallConfig.SWAY_AMOUNT_MIN;
        configData.swayAmountMax = SnowfallConfig.SWAY_AMOUNT_MAX;
        configData.swaySpeed = SnowfallConfig.SWAY_SPEED;
        configData.windStrength = Math.max(0.0f, SnowfallConfig.WIND_STRENGTH);
        configData.windAngle = normalizeAngle(SnowfallConfig.WIND_ANGLE);
        configData.rotationSpeed = SnowfallConfig.ROTATION_SPEED;
        configData.enableWeatherSound = SnowfallConfig.ENABLE_WEATHER_SOUND;
        configData.maxParticleCount = SnowfallConfig.MAX_PARTICLE_COUNT;
        configData.realisticSnow = SnowfallConfig.REALISTIC_SNOW;
    }

    /**
     * Apply current runtime values (use after modifying SnowfallConfig fields)
     */
    public static void applyAndSave() {
        captureFromRuntime();
        save();
        SnowSpawnManager.reload();
    }

    private static float normalizeAngle(float angle) {
        return (angle % 360.0f + 360.0f) % 360.0f;
    }

    /**
     * Apply a preset and save
     */
    public static void applyPreset(Preset preset) {
        switch (preset) {
            case LIGHT_SNOW:
                SnowfallConfig.applyLightSnowPreset();
                break;
            case BLIZZARD:
                SnowfallConfig.applyBlizzardPreset();
                break;
            case MAGICAL:
                SnowfallConfig.applyMagicalPreset();
                break;
            case CINEMATIC:
                SnowfallConfig.applyCinematicPreset();
                break;
        }
        applyAndSave();
    }

    /**
     * Get current config data for UI binding
     */
    public static SnowfallConfigData getConfig() {
        captureFromRuntime();
        return configData;
    }

    public enum Preset {
        LIGHT_SNOW,
        BLIZZARD,
        MAGICAL,
        CINEMATIC
    }
}