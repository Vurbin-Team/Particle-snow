package cinematic.snowstorm.utils;

import cinematic.snowstorm.config.SnowfallConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

/**
 * Single answer to "is it snowing here?" for snow, fog, sound and effects.
 */
public final class WeatherState {
    private static final float MIN_STRENGTH = 0.01f;

    private WeatherState() {}

    /**
     * 0..1 snow strength at {@code pos}: the vanilla rain level (it ramps up and down smoothly),
     * or 0 where the biome has no precipitation (desert, savanna, Nether, End...).
     */
    public static float strength(Level level, BlockPos pos) {
        float rain = level.getRainLevel(1.0f);
        if (rain < MIN_STRENGTH) return 0.0f;
        Biome.Precipitation precipitation = level.getBiome(pos).value().getPrecipitationAt(pos, level.getSeaLevel());
        if (precipitation == Biome.Precipitation.NONE) return 0.0f;
        if (SnowfallConfig.COLD_BIOMES_ONLY && precipitation != Biome.Precipitation.SNOW) return 0.0f;
        return rain;
    }

    public static boolean isSnowing(Level level, BlockPos pos) {
        return strength(level, pos) > 0.0f;
    }
}
