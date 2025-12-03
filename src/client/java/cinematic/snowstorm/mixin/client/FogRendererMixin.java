package cinematic.snowstorm.mixin.client;

import cinematic.snowstorm.fog.WeatherFogHandler;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.fog.FogRenderer;
import net.minecraft.client.world.ClientWorld;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin to modify fog rendering in FogRenderer for Minecraft 1.21.6+
 * In 1.21.6+, BackgroundRenderer was replaced with FogRenderer
 */
@Mixin(FogRenderer.class)
public class FogRendererMixin {

    /**
     * Inject into getFogColor to modify fog color
     */
    @Inject(
            method = "getFogColor",
            at = @At("RETURN"),
            cancellable = true
    )
    private void onGetFogColor(
            Camera camera,
            float tickProgress,
            ClientWorld world,
            int viewDistance,
            float skyDarkness,
            boolean thick,
            CallbackInfoReturnable<Vector4f> cir
    ) {
        // Update fog handler every frame
        WeatherFogHandler.updateFog();

        if (!WeatherFogHandler.shouldApplyWeatherFog()) {
            return;
        }

        // Get the current fog color
        Vector4f currentColor = cir.getReturnValue();

        // Get weather fog color multiplier
        float[] fogColor = WeatherFogHandler.getFogColor(false);

        // Apply color modification by multiplying with weather fog color
        Vector4f newColor = new Vector4f(
                currentColor.x * fogColor[0],
                currentColor.y * fogColor[1],
                currentColor.z * fogColor[2],
                currentColor.w
        );

        cir.setReturnValue(newColor);
    }

    /**
     * Modify environmentalStart value in applyFog method
     */
    @ModifyVariable(
            method = "applyFog(Ljava/nio/ByteBuffer;ILorg/joml/Vector4f;FFFFFF)V",
            at = @At("HEAD"),
            ordinal = 0,
            argsOnly = true
    )
    private float modifyEnvironmentalStart(float environmentalStart) {
        if (!WeatherFogHandler.shouldApplyWeatherFog()) {
            return environmentalStart;
        }
        return environmentalStart * WeatherFogHandler.getFogStart();
    }

    /**
     * Modify environmentalEnd value
     */
    @ModifyVariable(
            method = "applyFog(Ljava/nio/ByteBuffer;ILorg/joml/Vector4f;FFFFFF)V",
            at = @At("HEAD"),
            ordinal = 1,
            argsOnly = true
    )
    private float modifyEnvironmentalEnd(float environmentalEnd) {
        if (!WeatherFogHandler.shouldApplyWeatherFog()) {
            return environmentalEnd;
        }
        return environmentalEnd * WeatherFogHandler.getFogEnd();
    }

    /**
     * Modify renderDistanceStart value
     */
    @ModifyVariable(
            method = "applyFog(Ljava/nio/ByteBuffer;ILorg/joml/Vector4f;FFFFFF)V",
            at = @At("HEAD"),
            ordinal = 2,
            argsOnly = true
    )
    private float modifyRenderDistanceStart(float renderDistanceStart) {
        if (!WeatherFogHandler.shouldApplyWeatherFog()) {
            return renderDistanceStart;
        }
        return renderDistanceStart * WeatherFogHandler.getFogStart();
    }

    /**
     * Modify renderDistanceEnd value
     */
    @ModifyVariable(
            method = "applyFog(Ljava/nio/ByteBuffer;ILorg/joml/Vector4f;FFFFFF)V",
            at = @At("HEAD"),
            ordinal = 3,
            argsOnly = true
    )
    private float modifyRenderDistanceEnd(float renderDistanceEnd) {
        if (!WeatherFogHandler.shouldApplyWeatherFog()) {
            return renderDistanceEnd;
        }
        return renderDistanceEnd * WeatherFogHandler.getFogEnd();
    }

    /**
     * Modify skyEnd value
     */
    @ModifyVariable(
            method = "applyFog(Ljava/nio/ByteBuffer;ILorg/joml/Vector4f;FFFFFF)V",
            at = @At("HEAD"),
            ordinal = 4,
            argsOnly = true
    )
    private float modifySkyEnd(float skyEnd) {
        if (!WeatherFogHandler.shouldApplyWeatherFog()) {
            return skyEnd;
        }
        return skyEnd * WeatherFogHandler.getSkyFogEnd();
    }

    /**
     * Modify cloudEnd value
     */
    @ModifyVariable(
            method = "applyFog(Ljava/nio/ByteBuffer;ILorg/joml/Vector4f;FFFFFF)V",
            at = @At("HEAD"),
            ordinal = 5,
            argsOnly = true
    )
    private float modifyCloudEnd(float cloudEnd) {
        if (!WeatherFogHandler.shouldApplyWeatherFog()) {
            return cloudEnd;
        }
        return cloudEnd * WeatherFogHandler.getSkyFogEnd();
    }
}