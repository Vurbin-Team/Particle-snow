package cinematic.snowstorm.mixin.client;

import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {

    @Inject(method = "renderWeather", at = @At("HEAD"), cancellable = true)
    private void disableWeatherRendering(LightmapTextureManager manager, float tickDelta,
                                         double cameraX, double cameraY, double cameraZ,
                                         CallbackInfo ci) {
        // Отменяем рендеринг погоды
        ci.cancel();
    }
}