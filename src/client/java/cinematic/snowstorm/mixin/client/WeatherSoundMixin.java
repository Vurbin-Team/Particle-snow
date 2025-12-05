package cinematic.snowstorm.mixin.client;

import cinematic.snowstorm.sounds.ModSounds;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.sound.SoundEvent;
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
        Identifier soundId = sound.getId();

        // Заменяем звук дождя на звук метели/бури
        if (soundId.equals(SoundEvents.WEATHER_RAIN.getId()) ||
                soundId.equals(SoundEvents.WEATHER_RAIN_ABOVE.getId())) {

            ci.cancel(); // Отменяем оригинальный звук

            // Проверяем гроза или обычный дождь
            MinecraftClient client = MinecraftClient.getInstance();

            if (client.world != null && (client.world.isThundering() || client.world.isRaining())) {
                boolean isThundering = client.world.isThundering();

                // Выбираем звук: метель для дождя, буря для грозы
                SoundEvent newSound = isThundering ? ModSounds.BLIZZARD_HEAVY : ModSounds.BLIZZARD_LIGHT;

                // Создаём новый звук с фиксированными параметрами
                SoundInstance replacement = PositionedSoundInstance.ambient(
                        newSound,
                        1.0f,  // Громкость (контролируется ползунком "Погода")
                        1.0f   // Высота тона
                );

                // Воспроизводим замену
                ((SoundManager)(Object)this).play(replacement);
            }
        }
    }
}