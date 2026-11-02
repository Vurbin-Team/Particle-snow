package cinematic.snowstorm.sounds;

import cinematic.snowstorm.config.SnowfallConfig;
import cinematic.snowstorm.effects.WindGusts;
import cinematic.snowstorm.utils.WeatherState;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.Heightmap;

public class AdvancedWeatherSoundManager {

    // Blocks of cover above the player's head from which a covered spot counts as a cave (silent).
    private static final int CAVE_DEPTH = 8;

    private static WeatherSound currentSound = null;
    private static boolean currentHeavy = false;

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.level == null || client.player == null) {
                // Left the world: the engine dropped the sound, forget it so the next world starts a new one.
                stopSound(client);
                return;
            }

            // Desired state is recomputed every tick (no edge detection), so toggling the sound option
            // or joining another world in the rain always ends up with the right sound playing.
            boolean snowing = SnowfallConfig.ENABLE_WEATHER_SOUND
                    && WeatherState.isSnowing(client.level, client.player.blockPosition());
            boolean heavy = client.level.isThundering();

            if (currentSound != null && (currentSound.isStopped() || !snowing || heavy != currentHeavy)) {
                currentSound.fadeOut();
                currentSound = null;
            }
            if (snowing && currentSound == null) {
                startSound(client, heavy);
            }
        });
    }

    private static void startSound(Minecraft client, boolean isThundering) {
        SoundEvent sound = isThundering ? ModSounds.BLIZZARD_HEAVY : ModSounds.BLIZZARD_LIGHT;
        currentHeavy = isThundering;
        currentSound = new WeatherSound(sound, client);

        client.getSoundManager().play(currentSound);
    }

    private static void stopSound(Minecraft client) {
        if (currentSound != null && client.getSoundManager() != null) {
            currentSound.stopSound();
            client.getSoundManager().stop(currentSound);
        }
        currentSound = null;
    }

    // Кастомный звук с динамической громкостью (наследует MovingSoundInstance)
    private static class WeatherSound extends AbstractTickableSoundInstance {
        private final Minecraft client;
        private boolean fading = false;
        private static final float SMOOTHING_FACTOR = 0.1f; // Скорость сглаживания (0.1 = плавно, 1.0 = мгновенно)

        public WeatherSound(SoundEvent sound, Minecraft client) {
            super(sound, SoundSource.WEATHER, RandomSource.create());
            this.client = client;
            this.looping = true;
            this.delay = 0;
            // Fades in. Not 0: the engine skips sounds that start silent and never ticks them.
            this.volume = 0.001f;
            this.pitch = 1.0f;
            this.x = 0;
            this.y = 0;
            this.z = 0;
            this.relative = true;
            this.attenuation = Attenuation.NONE;
        }

        @Override
        public void tick() {
            if (client.player == null || client.level == null) {
                this.stop();
                return;
            }

            float targetVolume = fading ? 0.0f : targetVolume();

            // Плавно приближаем текущую громкость к целевой
            this.volume += (targetVolume - this.volume) * SMOOTHING_FACTOR;

            // Faded out: the sound is really over now.
            if (fading && this.volume < 0.01f) {
                this.stop();
            }
        }

        private float targetVolume() {
            BlockPos pos = client.player.blockPosition();
            float volume;
            if (client.level.canSeeSky(pos)) {
                volume = 1.0f;
            } else {
                int surface = client.level.getHeight(Heightmap.Types.MOTION_BLOCKING, pos.getX(), pos.getZ());
                volume = pos.getY() < surface - CAVE_DEPTH ? 0.0f : 0.15f;
            }
            return volume * WindGusts.getSoundVolumeFactor();
        }

        /** Lets the sound fade out and stop itself. */
        public void fadeOut() {
            this.fading = true;
        }

        /** Stops at once (leaving the world). */
        public void stopSound() {
            this.stop();
        }
    }
}
