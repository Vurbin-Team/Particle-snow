package cinematic.snowstorm.mixin.client;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WeatherRendering;
import net.minecraft.client.render.state.WeatherRenderState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Mixin to disable vanilla rain/snow particles (precipitation)
 * This allows your custom snow particles to be the only visible weather effect
 */
@Mixin(WeatherRendering.class)
public class DisableRainParticlesMixin {

    /**
     * Cancel the renderPrecipitation method which spawns rain/snow particles
     * This is the method that renders falling rain/snow and splash effects
     */
    @Inject(
            method = "renderPrecipitation",
            at = @At("HEAD"),
            cancellable = true
    )
    private void cancelPrecipitationParticles(
            VertexConsumerProvider vertexConsumers, Vec3d pos, WeatherRenderState weatherRenderState, CallbackInfo ci
    ) {
        // Cancel the entire method - no vanilla precipitation particles will render
        ci.cancel();
    }
}