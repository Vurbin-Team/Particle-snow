package cinematic.snowstorm.effects;

import cinematic.snowstorm.config.SnowfallConfig;

/**
 * Wind gusts: a slowly pulsing extra wind vector that is added on top of the configured wind.
 * The configured WIND_STRENGTH / WIND_ANGLE are never modified (so they are saved untouched);
 * snow particles just add {@link #getGustX()} / {@link #getGustZ()} to their per-tick movement.
 */
public final class WindGusts {
    // Gust speed in blocks per tick at GUST_STRENGTH = 1.0.
    private static final double BASE_AMPLITUDE = 0.03;
    // Fade-in after snow starts / the effect is enabled, so gusts never pop in.
    private static final int RAMP_TICKS = 60;

    private static double phase;
    private static int ramp;
    private static float gustX;
    private static float gustZ;
    // 0..1: how strong the current gust is, independent of GUST_STRENGTH. Drives the wind sound.
    private static float intensity;

    private WindGusts() {}

    public static void tick(boolean snowing) {
        if (!SnowfallConfig.ENABLE_WIND_GUSTS || !snowing) {
            gustX = 0f;
            gustZ = 0f;
            intensity = 0f;
            ramp = 0;
            return;
        }
        if (ramp < RAMP_TICKS) ramp++;

        // Phase accumulates, so changing the period in the menu never makes the gust jump.
        double periodTicks = Math.max(3.0, SnowfallConfig.GUST_PERIOD_SECONDS) * 20.0;
        phase += Math.PI * 2.0 / periodTicks;
        if (phase > Math.PI * 200.0) phase -= Math.PI * 200.0;

        // Two layers with unrelated periods: no obviously repeating pattern.
        double mix = 0.65 * Math.sin(phase) + 0.35 * Math.sin(phase * 2.7 + 1.3);
        double gust = Math.max(0.0, mix);
        gust *= gust; // short sharp gusts with calm in between

        // Gusts also swing a little around the configured wind direction.
        double angle = Math.toRadians(SnowfallConfig.WIND_ANGLE) + 0.35 * Math.sin(phase * 0.6 + 0.7);
        intensity = (float) (gust * ramp / RAMP_TICKS);
        double amplitude = BASE_AMPLITUDE * Math.max(0.0, SnowfallConfig.GUST_STRENGTH)
                * gust * ((double) ramp / RAMP_TICKS);
        gustX = (float) (Math.cos(angle) * amplitude);
        gustZ = (float) (Math.sin(angle) * amplitude);
    }

    public static float getGustX() {
        return gustX;
    }

    public static float getGustZ() {
        return gustZ;
    }

    /** Wind sound volume multiplier: 0.7 in a lull, 1.0 at the peak of a gust, 1.0 with gusts off. */
    public static float getSoundVolumeFactor() {
        return SnowfallConfig.ENABLE_WIND_GUSTS ? 0.7f + 0.3f * intensity : 1.0f;
    }
}
