package cinematic.snowstorm.mixin.client;

import net.minecraft.client.renderer.WeatherEffectRenderer;
import net.minecraft.client.renderer.state.level.WeatherRenderState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to disable vanilla rain/snow particles (precipitation)
 * This allows your custom snow particles to be the only visible weather effect
 */
@Mixin(WeatherEffectRenderer.class)
public class DisableRainParticlesMixin {

    /**
     * Cancel weather rendering, which draws vanilla rain and snow
     * This is the method that renders falling rain/snow and splash effects
     */
    @Inject(
            method = "render",
            at = @At("HEAD"),
            cancellable = true
    )
    private void cancelPrecipitationParticles(
            Vec3 pos, WeatherRenderState weatherRenderState, CallbackInfo ci
    ) {
        // Cancel the entire method - no vanilla precipitation particles will render
        ci.cancel();
    }
}