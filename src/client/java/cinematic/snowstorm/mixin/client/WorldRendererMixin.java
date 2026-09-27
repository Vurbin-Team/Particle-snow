package cinematic.snowstorm.mixin.client;

import net.minecraft.client.renderer.WeatherEffectRenderer;
import net.minecraft.client.renderer.state.level.WeatherRenderState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WeatherEffectRenderer.class)
public class WorldRendererMixin {

    @Inject(method = "render(Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/client/renderer/state/level/WeatherRenderState;)V",
            at = @At("HEAD"),
            cancellable = true)
    private void disableWeatherRendering(Vec3 cameraPosition, WeatherRenderState state, CallbackInfo ci) {
        ci.cancel();
    }
}