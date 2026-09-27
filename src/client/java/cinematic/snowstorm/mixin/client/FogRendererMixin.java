package cinematic.snowstorm.mixin.client;

import cinematic.snowstorm.fog.WeatherFogHandler;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.FogRenderer;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FogRenderer.class)
public class FogRendererMixin {

    @Inject(method = "setupFog", at = @At("RETURN"))
    private void applyWeatherFog(CallbackInfoReturnable<FogData> cir) {
        WeatherFogHandler.updateFog();
        if (!WeatherFogHandler.shouldApplyWeatherFog()) {
            return;
        }

        FogData fog = cir.getReturnValue();
        fog.environmentalStart *= WeatherFogHandler.getFogStart();
        fog.environmentalEnd *= WeatherFogHandler.getFogEnd();
        fog.renderDistanceStart *= WeatherFogHandler.getFogStart();
        fog.renderDistanceEnd *= WeatherFogHandler.getFogEnd();
        fog.skyEnd *= WeatherFogHandler.getSkyFogEnd();
        fog.cloudEnd *= WeatherFogHandler.getSkyFogEnd();

        float[] colorMultiplier = WeatherFogHandler.getFogColor(false);
        Vector4f color = fog.color;
        color.set(
                color.x * colorMultiplier[0],
                color.y * colorMultiplier[1],
                color.z * colorMultiplier[2],
                color.w
        );
    }
}
