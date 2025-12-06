package cinematic.snowstorm.sounds;

import cinematic.snowstorm.config.SnowfallConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.MovingSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.random.Random;

public class AdvancedWeatherSoundManager {

    private static WeatherSound currentSound = null;
    private static boolean wasRaining = false;
    private static boolean wasThundering = false;

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!SnowfallConfig.ENABLE_WEATHER_SOUND || client.world == null || client.player == null) {
                stopSound(client);
                return;
            }

            boolean isRaining = client.world.isRaining();
            boolean isThundering = client.world.isThundering();

            // Если погода началась или изменился тип (дождь/гроза)
            if (isRaining && (!wasRaining || isThundering != wasThundering)) {
                stopSound(client);
                startSound(client, isThundering);
            }
            // Если погода закончилась
            else if (!isRaining && wasRaining) {
                stopSound(client);
            }

            wasRaining = isRaining;
            wasThundering = isThundering;
        });
    }

    private static void startSound(MinecraftClient client, boolean isThundering) {
        if (currentSound != null) return;

        SoundEvent sound = isThundering ? ModSounds.BLIZZARD_HEAVY : ModSounds.BLIZZARD_LIGHT;
        currentSound = new WeatherSound(sound, client);

        client.getSoundManager().play(currentSound);
    }

    private static void stopSound(MinecraftClient client) {
        if (currentSound != null && client.getSoundManager() != null) {
            currentSound.stopSound();
            client.getSoundManager().stop(currentSound);
            currentSound = null;
        }
    }

    // Кастомный звук с динамической громкостью (наследует MovingSoundInstance)
    private static class WeatherSound extends MovingSoundInstance {
        private final MinecraftClient client;
        private boolean shouldStop = false;
        private float targetVolume = 1.0f;
        private static final float SMOOTHING_FACTOR = 0.1f; // Скорость сглаживания (0.1 = плавно, 1.0 = мгновенно)

        public WeatherSound(SoundEvent sound, MinecraftClient client) {
            super(sound, SoundCategory.WEATHER, Random.create());
            this.client = client;
            this.repeat = true;
            this.repeatDelay = 0;
            this.volume = 1.0f;
            this.pitch = 1.0f;
            this.x = 0;
            this.y = 0;
            this.z = 0;
            this.relative = true;
            this.attenuationType = AttenuationType.NONE;
        }

        @Override
        public void tick() {
            if (shouldStop || client.player == null || client.world == null) {
                this.setDone();
                return;
            }

            // Определяем целевую громкость
            boolean canSeeSky = client.world.isSkyVisible(client.player.getBlockPos());
            boolean disableSound = client.player.getBlockPos().getY() < 60 && !client.world.isSkyVisible(client.player.getBlockPos());

            if(disableSound){
                targetVolume = 0f;
            }
            else
            {
                targetVolume = canSeeSky ? 1.0f : 0.15f;
            }

            // Плавно приближаем текущую громкость к целевой
            this.volume += (targetVolume - this.volume) * SMOOTHING_FACTOR;
        }

        public void stopSound() {
            this.shouldStop = true;
        }
    }
}