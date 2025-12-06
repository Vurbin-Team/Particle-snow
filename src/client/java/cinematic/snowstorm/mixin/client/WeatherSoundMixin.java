package cinematic.snowstorm.mixin.client;

import cinematic.snowstorm.config.SnowfallConfig;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SoundManager.class)
public class WeatherSoundMixin {

    @Inject(method = "play(Lnet/minecraft/client/sound/SoundInstance;)V", at = @At("HEAD"), cancellable = true)
    private void replaceRainWithBlizzard(SoundInstance sound, CallbackInfo ci) {
        if (!SnowfallConfig.ENABLE_WEATHER_SOUND) return;

        Identifier soundId = sound.getId();

        // Полностью блокируем оригинальные звуки дождя
        // Наш менеджер сам будет управлять звуками
        if (soundId.equals(SoundEvents.WEATHER_RAIN.getId()) ||
                soundId.equals(SoundEvents.WEATHER_RAIN_ABOVE.getId())) {
            ci.cancel();
        }
    }
}