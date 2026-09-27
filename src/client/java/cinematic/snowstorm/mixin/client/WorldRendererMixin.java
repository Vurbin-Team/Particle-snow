package cinematic.snowstorm.mixin.client;

import com.mojang.renderpearl.api.commands.RenderPass;
import net.minecraft.client.renderer.WeatherEffectRenderer;
import net.minecraft.client.renderer.oit.OitStage;
import net.minecraft.client.renderer.state.level.WeatherRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WeatherEffectRenderer.class)
public class WorldRendererMixin {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void disableWeatherRendering(WeatherRenderState state, RenderPass renderPass, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "renderOit", at = @At("HEAD"), cancellable = true)
    private void disableOitWeatherRendering(
            OitStage stage,
            WeatherRenderState state,
            RenderPass renderPass,
            CallbackInfo ci
    ) {
        ci.cancel();
    }
}