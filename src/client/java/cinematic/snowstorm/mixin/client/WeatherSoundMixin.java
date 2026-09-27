package cinematic.snowstorm.mixin.client;

import cinematic.snowstorm.config.SnowfallConfig;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SoundEngine.class)
public class WeatherSoundMixin {

    @Inject(method = "play", at = @At("HEAD"), cancellable = true)
    private void replaceRainWithBlizzard(SoundInstance sound, CallbackInfoReturnable<SoundEngine.PlayResult> ci) {
        if (!SnowfallConfig.ENABLE_WEATHER_SOUND) return;

        Identifier soundId = sound.getIdentifier();

        // Полностью блокируем оригинальные звуки дождя
        // Наш менеджер сам будет управлять звуками
        if (soundId.equals(SoundEvents.WEATHER_RAIN.location()) ||
                soundId.equals(SoundEvents.WEATHER_RAIN_ABOVE.location())) {
            ci.cancel();
        }
    }
}