package cinematic.snowstorm.modMenu;

import cinematic.snowstorm.config.SnowfallConfig;
import cinematic.snowstorm.config.SnowfallConfigData;
import cinematic.snowstorm.config.SnowfallConfigManager;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.text.Text;

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
                    .setTitle(Text.literal("Snowstorm Configuration"))
                    .setSavingRunnable(SnowfallConfigManager::applyAndSave);

            ConfigEntryBuilder entryBuilder = builder.entryBuilder();
            SnowfallConfigData config = SnowfallConfigManager.getConfig();

            // ==== PRESETS CATEGORY ====
            ConfigCategory presets = builder.getOrCreateCategory(Text.literal("Presets"));

            presets.addEntry(entryBuilder.startTextDescription(
                    Text.literal("Select a preset to quickly configure snowfall style")
            ).build());

            // Preset selector using enum
            presets.addEntry(entryBuilder.startEnumSelector(
                            Text.literal("Snow Preset"),
                            SnowfallConfigManager.Preset.class,
                            SnowfallConfigManager.Preset.CINEMATIC
                    )
                    .setDefaultValue(SnowfallConfigManager.Preset.CINEMATIC)
                    .setTooltip(
                            Text.literal("LIGHT_SNOW: Gentle snowfall"),
                            Text.literal("BLIZZARD: Heavy snowstorm"),
                            Text.literal("MAGICAL: Slow-motion effect"),
                            Text.literal("CINEMATIC: Wide-area snow")
                    )
                    .setSaveConsumer(SnowfallConfigManager::applyPreset)
                    .build());

            presets.addEntry(entryBuilder.startTextDescription(
                    Text.literal("§7After selecting a preset, click 'Done' to apply and save")
            ).build());

            // ==== SPAWN SETTINGS ====
            ConfigCategory spawn = builder.getOrCreateCategory(Text.literal("Spawn Settings"));

            spawn.addEntry(entryBuilder.startIntSlider(
                            Text.literal("Spawn Height"),
                            config.spawnHeight, 20, 80)
                    .setDefaultValue(40)
                    .setTooltip(Text.literal("How high above player particles spawn"))
                    .setSaveConsumer(val -> SnowfallConfig.SPAWN_HEIGHT = val)
                    .build());

            spawn.addEntry(entryBuilder.startIntSlider(
                            Text.literal("Spawn Radius"),
                            config.spawnRadius, 40, 150)
                    .setDefaultValue(80)
                    .setTooltip(Text.literal("Horizontal radius for close particles"))
                    .setSaveConsumer(val -> SnowfallConfig.SPAWN_RADIUS = val)
                    .build());

            spawn.addEntry(entryBuilder.startIntSlider(
                            Text.literal("Far Spawn Radius"),
                            config.farSpawnRadius, 80, 200)
                    .setDefaultValue(120)
                    .setTooltip(Text.literal("Distance for far particles (depth effect)"))
                    .setSaveConsumer(val -> SnowfallConfig.FAR_SPAWN_RADIUS = val)
                    .build());

            spawn.addEntry(entryBuilder.startFloatField(
                            Text.literal("Far Spawn Chance"),
                            config.farSpawnChance)
                    .setDefaultValue(0.3f)
                    .setTooltip(Text.literal("Chance for particles to spawn far (0.0 - 1.0)"))
                    .setSaveConsumer(val -> SnowfallConfig.FAR_SPAWN_CHANCE = val)
                    .build());

            spawn.addEntry(entryBuilder.startIntSlider(
                            Text.literal("Particles Per Tick"),
                            config.particlesPerTick, 20, 300)
                    .setDefaultValue(200)
                    .setTooltip(Text.literal("Higher = denser snowfall. Light: 60-80, Heavy: 180-220"))
                    .setSaveConsumer(val -> SnowfallConfig.PARTICLES_PER_TICK = val)
                    .build());

            spawn.addEntry(entryBuilder.startIntSlider(
                            Text.literal("Spawn Interval"),
                            config.spawnInterval, 1, 10)
                    .setDefaultValue(3)
                    .setTooltip(Text.literal("Ticks between spawn cycles (lower = more frequent)"))
                    .setSaveConsumer(val -> SnowfallConfig.SPAWN_INTERVAL = val)
                    .build());

            // ==== MOVEMENT SETTINGS ====
            ConfigCategory movement = builder.getOrCreateCategory(Text.literal("Movement"));

            movement.addEntry(entryBuilder.startFloatField(
                            Text.literal("Player Follow Strength"),
                            config.playerFollowStrength)
                    .setDefaultValue(0.015f)
                    .setTooltip(Text.literal("How much particles follow player (0.0 - 1.0)"))
                    .setSaveConsumer(val -> SnowfallConfig.PLAYER_FOLLOW_STRENGTH = val)
                    .build());

            movement.addEntry(entryBuilder.startFloatField(
                            Text.literal("Air Drag"),
                            config.airDrag)
                    .setDefaultValue(0.98f)
                    .setTooltip(Text.literal("Air resistance (0.95 = high, 0.99 = low)"))
                    .setSaveConsumer(val -> SnowfallConfig.AIR_DRAG = val)
                    .build());

            movement.addEntry(entryBuilder.startFloatField(
                            Text.literal("Fall Speed Min"),
                            config.fallSpeedMin)
                    .setDefaultValue(0.08f)
                    .setTooltip(Text.literal("Minimum fall speed"))
                    .setSaveConsumer(val -> SnowfallConfig.FALL_SPEED_MIN = val)
                    .build());

            movement.addEntry(entryBuilder.startFloatField(
                            Text.literal("Fall Speed Max"),
                            config.fallSpeedMax)
                    .setDefaultValue(0.10f)
                    .setTooltip(Text.literal("Maximum fall speed"))
                    .setSaveConsumer(val -> SnowfallConfig.FALL_SPEED_MAX = val)
                    .build());

            // ==== VISUAL SETTINGS ====
            ConfigCategory visual = builder.getOrCreateCategory(Text.literal("Visual"));

            visual.addEntry(entryBuilder.startFloatField(
                            Text.literal("Size Min"),
                            config.sizeMin)
                    .setDefaultValue(0.18f)
                    .setTooltip(Text.literal("Minimum particle size"))
                    .setSaveConsumer(val -> SnowfallConfig.SIZE_MIN = val)
                    .build());

            visual.addEntry(entryBuilder.startFloatField(
                            Text.literal("Size Max"),
                            config.sizeMax)
                    .setDefaultValue(0.25f)
                    .setTooltip(Text.literal("Maximum particle size"))
                    .setSaveConsumer(val -> SnowfallConfig.SIZE_MAX = val)
                    .build());

            visual.addEntry(entryBuilder.startFloatField(
                            Text.literal("Alpha Min"),
                            config.alphaMin)
                    .setDefaultValue(0.30f)
                    .setTooltip(Text.literal("Minimum transparency"))
                    .setSaveConsumer(val -> SnowfallConfig.ALPHA_MIN = val)
                    .build());

            visual.addEntry(entryBuilder.startFloatField(
                            Text.literal("Alpha Max"),
                            config.alphaMax)
                    .setDefaultValue(0.45f)
                    .setTooltip(Text.literal("Maximum transparency"))
                    .setSaveConsumer(val -> SnowfallConfig.ALPHA_MAX = val)
                    .build());

            // ==== WIND & SWAY ====
            ConfigCategory windSway = builder.getOrCreateCategory(Text.literal("Wind & Sway"));

            windSway.addEntry(entryBuilder.startFloatField(
                            Text.literal("Sway Amount Min"),
                            config.swayAmountMin)
                    .setDefaultValue(0.018f)
                    .setTooltip(Text.literal("Minimum swaying amplitude"))
                    .setSaveConsumer(val -> SnowfallConfig.SWAY_AMOUNT_MIN = val)
                    .build());

            windSway.addEntry(entryBuilder.startFloatField(
                            Text.literal("Sway Amount Max"),
                            config.swayAmountMax)
                    .setDefaultValue(0.043f)
                    .setTooltip(Text.literal("Maximum swaying amplitude"))
                    .setSaveConsumer(val -> SnowfallConfig.SWAY_AMOUNT_MAX = val)
                    .build());

            windSway.addEntry(entryBuilder.startFloatField(
                            Text.literal("Sway Speed"),
                            config.swaySpeed)
                    .setDefaultValue(0.4f)
                    .setTooltip(Text.literal("Speed of swaying motion"))
                    .setSaveConsumer(val -> SnowfallConfig.SWAY_SPEED = val)
                    .build());

            windSway.addEntry(entryBuilder.startFloatField(
                            Text.literal("Wind Min"),
                            config.windMin)
                    .setDefaultValue(0.5f)
                    .setTooltip(Text.literal("Minimum wind strength"))
                    .setSaveConsumer(val -> SnowfallConfig.WIND_MIN = val)
                    .build());

            windSway.addEntry(entryBuilder.startFloatField(
                            Text.literal("Wind Max"),
                            config.windMax)
                    .setDefaultValue(1.0f)
                    .setTooltip(Text.literal("Maximum wind strength"))
                    .setSaveConsumer(val -> SnowfallConfig.WIND_MAX = val)
                    .build());

            windSway.addEntry(entryBuilder.startFloatField(
                            Text.literal("Rotation Speed"),
                            config.rotationSpeed)
                    .setDefaultValue(0.02f)
                    .setTooltip(Text.literal("Speed of particle rotation"))
                    .setSaveConsumer(val -> SnowfallConfig.ROTATION_SPEED = val)
                    .build());

            return builder.build();
        };
    }
}