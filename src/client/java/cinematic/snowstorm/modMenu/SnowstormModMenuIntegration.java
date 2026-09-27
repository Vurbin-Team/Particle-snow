package cinematic.snowstorm.modMenu;

import cinematic.snowstorm.config.SnowfallConfig;
import cinematic.snowstorm.config.SnowfallConfigData;
import cinematic.snowstorm.config.SnowfallConfigManager;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.network.chat.Component;

/**
 * Mod Menu integration for Snowstorm configuration
 * Requires: Mod Menu 7.1.0 and Cloth Config API
 */
public class SnowstormModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            // Apply and save - this ensures all changes are captured
            ConfigBuilder builder = ConfigBuilder.create()
                    .setParentScreen(parent)
                    .setTitle(Component.literal("Snowstorm Configuration"))
                    .setSavingRunnable(SnowfallConfigManager::applyAndSave);

            ConfigEntryBuilder entryBuilder = builder.entryBuilder();
            SnowfallConfigData config = SnowfallConfigManager.getConfig();

            // ==== PRESETS CATEGORY ====
            ConfigCategory presets = builder.getOrCreateCategory(Component.literal("Presets"));

            presets.addEntry(entryBuilder.startTextDescription(
                    Component.literal("Select a preset to quickly configure snowfall style")
            ).build());

            // Preset selector using enum
            presets.addEntry(entryBuilder.startEnumSelector(
                            Component.literal("Snow Preset"),
                            SnowfallConfigManager.Preset.class,
                            SnowfallConfigManager.Preset.CINEMATIC
                    )
                    .setDefaultValue(SnowfallConfigManager.Preset.CINEMATIC)
                    .setTooltip(
                            Component.literal("LIGHT_SNOW: Gentle snowfall"),
                            Component.literal("BLIZZARD: Heavy snowstorm"),
                            Component.literal("MAGICAL: Slow-motion effect"),
                            Component.literal("CINEMATIC: Wide-area snow")
                    )
                    .setSaveConsumer(SnowfallConfigManager::applyPreset)
                    .build());

            presets.addEntry(entryBuilder.startBooleanToggle(
                            Component.literal("Weather Sound"),
                            config.enableWeatherSound)
                    .setDefaultValue(true)
                    .setTooltip(Component.literal("Sound of snowy weather"))
                    .setSaveConsumer(val -> SnowfallConfig.ENABLE_WEATHER_SOUND = val)
                    .build());

            presets.addEntry(entryBuilder.startIntField(Component.literal("Particle max count"), config.maxParticleCount)
                    .setDefaultValue(32000)
                    .setTooltip(Component.literal("Maximum active snow particles"))
                    .setSaveConsumer(val -> SnowfallConfig.MAX_PARTICLE_COUNT = val)
                    .build());

            presets.addEntry(entryBuilder.startTextDescription(
                    Component.literal("§7After selecting a preset, click 'Done' to apply and save")
            ).build());

            // ==== TEXTURES ====
            ConfigCategory textures = builder.getOrCreateCategory(Component.literal("Textures"));

            textures.addEntry(entryBuilder.startBooleanToggle(
                            Component.literal("Realistic Snow"),
                            config.realisticSnow)
                    .setDefaultValue(false)
                    .setTooltip(
                            Component.literal("Off: stylized pixel-art snowflakes (default)"),
                            Component.literal("On: soft, photo-realistic snow particles")
                    )
                    .setSaveConsumer(val -> SnowfallConfig.REALISTIC_SNOW = val)
                    .build());

            // ==== SPAWN SETTINGS ====
            ConfigCategory spawn = builder.getOrCreateCategory(Component.literal("Spawn Settings"));

            spawn.addEntry(entryBuilder.startIntSlider(
                            Component.literal("Spawn Height"),
                            config.spawnHeight, 20, 80)
                    .setDefaultValue(40)
                    .setTooltip(Component.literal("How high above player particles spawn"))
                    .setSaveConsumer(val -> SnowfallConfig.SPAWN_HEIGHT = val)
                    .build());

            spawn.addEntry(entryBuilder.startIntSlider(
                            Component.literal("Spawn Radius"),
                            config.spawnRadius, 40, 150)
                    .setDefaultValue(80)
                    .setTooltip(Component.literal("Horizontal radius for close particles"))
                    .setSaveConsumer(val -> SnowfallConfig.SPAWN_RADIUS = val)
                    .build());

            spawn.addEntry(entryBuilder.startIntSlider(
                            Component.literal("Far Spawn Radius"),
                            config.farSpawnRadius, 80, 200)
                    .setDefaultValue(120)
                    .setTooltip(Component.literal("Distance for far particles (depth effect)"))
                    .setSaveConsumer(val -> SnowfallConfig.FAR_SPAWN_RADIUS = val)
                    .build());

            spawn.addEntry(entryBuilder.startFloatField(
                            Component.literal("Far Spawn Chance"),
                            config.farSpawnChance)
                    .setDefaultValue(0.3f)
                    .setTooltip(Component.literal("Chance for particles to spawn far (0.0 - 1.0)"))
                    .setSaveConsumer(val -> SnowfallConfig.FAR_SPAWN_CHANCE = val)
                    .build());

            spawn.addEntry(entryBuilder.startIntSlider(
                            Component.literal("Particles Per Tick"),
                            config.particlesPerTick, 20, 500)
                    .setDefaultValue(150)
                    .setTooltip(Component.literal("Higher = denser snowfall. Light: 60-80, Heavy: 180-220"))
                    .setSaveConsumer(val -> SnowfallConfig.PARTICLES_PER_TICK = val)
                    .build());

            spawn.addEntry(entryBuilder.startIntSlider(
                            Component.literal("Spawn Interval"),
                            config.spawnInterval, 1, 10)
                    .setDefaultValue(3)
                    .setTooltip(Component.literal("Ticks between spawn cycles (lower = more frequent)"))
                    .setSaveConsumer(val -> SnowfallConfig.SPAWN_INTERVAL = val)
                    .build());

            // ==== MOVEMENT SETTINGS ====
            ConfigCategory movement = builder.getOrCreateCategory(Component.literal("Movement"));

            movement.addEntry(entryBuilder.startFloatField(
                            Component.literal("Player Follow Strength"),
                            config.playerFollowStrength)
                    .setDefaultValue(0.015f)
                    .setTooltip(Component.literal("How much particles follow player (0.0 - 1.0)"))
                    .setSaveConsumer(val -> SnowfallConfig.PLAYER_FOLLOW_STRENGTH = val)
                    .build());

            movement.addEntry(entryBuilder.startFloatField(
                            Component.literal("Fall Speed Min"),
                            config.fallSpeedMin)
                    .setDefaultValue(0.08f)
                    .setTooltip(Component.literal("Minimum fall speed"))
                    .setSaveConsumer(val -> SnowfallConfig.FALL_SPEED_MIN = val)
                    .build());

            movement.addEntry(entryBuilder.startFloatField(
                            Component.literal("Fall Speed Max"),
                            config.fallSpeedMax)
                    .setDefaultValue(0.10f)
                    .setTooltip(Component.literal("Maximum fall speed"))
                    .setSaveConsumer(val -> SnowfallConfig.FALL_SPEED_MAX = val)
                    .build());

            // ==== VISUAL SETTINGS ====
            ConfigCategory visual = builder.getOrCreateCategory(Component.literal("Visual"));

            visual.addEntry(entryBuilder.startFloatField(
                            Component.literal("Size Min"),
                            config.sizeMin)
                    .setDefaultValue(0.18f)
                    .setTooltip(Component.literal("Minimum particle size"))
                    .setSaveConsumer(val -> SnowfallConfig.SIZE_MIN = val)
                    .build());

            visual.addEntry(entryBuilder.startFloatField(
                            Component.literal("Size Max"),
                            config.sizeMax)
                    .setDefaultValue(0.25f)
                    .setTooltip(Component.literal("Maximum particle size"))
                    .setSaveConsumer(val -> SnowfallConfig.SIZE_MAX = val)
                    .build());

            visual.addEntry(entryBuilder.startFloatField(
                            Component.literal("Alpha Min"),
                            config.alphaMin)
                    .setDefaultValue(0.30f)
                    .setTooltip(Component.literal("Minimum transparency"))
                    .setSaveConsumer(val -> SnowfallConfig.ALPHA_MIN = val)
                    .build());

            visual.addEntry(entryBuilder.startFloatField(
                            Component.literal("Alpha Max"),
                            config.alphaMax)
                    .setDefaultValue(0.45f)
                    .setTooltip(Component.literal("Maximum transparency"))
                    .setSaveConsumer(val -> SnowfallConfig.ALPHA_MAX = val)
                    .build());

            // ==== WIND & SWAY ====
            ConfigCategory windSway = builder.getOrCreateCategory(Component.literal("Wind & Sway"));

            windSway.addEntry(entryBuilder.startFloatField(
                            Component.literal("Sway Amount Min"),
                            config.swayAmountMin)
                    .setDefaultValue(0.018f)
                    .setTooltip(Component.literal("Minimum swaying amplitude"))
                    .setSaveConsumer(val -> SnowfallConfig.SWAY_AMOUNT_MIN = val)
                    .build());

            windSway.addEntry(entryBuilder.startFloatField(
                            Component.literal("Sway Amount Max"),
                            config.swayAmountMax)
                    .setDefaultValue(0.043f)
                    .setTooltip(Component.literal("Maximum swaying amplitude"))
                    .setSaveConsumer(val -> SnowfallConfig.SWAY_AMOUNT_MAX = val)
                    .build());

            windSway.addEntry(entryBuilder.startFloatField(
                            Component.literal("Sway Speed"),
                            config.swaySpeed)
                    .setDefaultValue(0.4f)
                    .setTooltip(Component.literal("Speed of swaying motion"))
                    .setSaveConsumer(val -> SnowfallConfig.SWAY_SPEED = val)
                    .build());

            windSway.addEntry(entryBuilder.startFloatField(
                            Component.literal("Wind Strength"),
                            config.windStrength)
                    .setDefaultValue(0.02f)
                    .setTooltip(Component.literal("Immediate horizontal particle speed in blocks per tick"))
                    .setSaveConsumer(val -> SnowfallConfig.WIND_STRENGTH = Math.max(0.0f, val))
                    .build());

            windSway.addEntry(entryBuilder.startFloatField(
                            Component.literal("Wind Angle"),
                            config.windAngle)
                    .setDefaultValue(0.0f)
                    .setTooltip(Component.literal("Direction in degrees: 0 = +X, 90 = +Z"))
                    .setSaveConsumer(val -> SnowfallConfig.WIND_ANGLE = (val % 360.0f + 360.0f) % 360.0f)
                    .build());

            windSway.addEntry(entryBuilder.startFloatField(
                            Component.literal("Rotation Speed"),
                            config.rotationSpeed)
                    .setDefaultValue(0.02f)
                    .setTooltip(Component.literal("Speed of particle rotation"))
                    .setSaveConsumer(val -> SnowfallConfig.ROTATION_SPEED = val)
                    .build());

            return builder.build();
        };
    }
}