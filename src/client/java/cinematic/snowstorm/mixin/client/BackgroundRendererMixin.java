package cinematic.snowstorm.mixin.client;

import cinematic.snowstorm.fog.WeatherFogHandler;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to modify fog rendering in BackgroundRenderer
 */
@Mixin(BackgroundRenderer.class)
public class BackgroundRendererMixin {

    /**
     * Inject at the start of applyFog method to modify fog parameters
     */
    @Inject(
            method = "applyFog",
            at = @At("TAIL")
    )
    private static void onApplyFog(
            Camera camera,
            BackgroundRenderer.FogType fogType,
            float viewDistance,
            boolean thickFog,
            float tickDelta,
            CallbackInfo ci
    ) {
        // Update fog handler
        WeatherFogHandler.updateFog();

        // Only apply weather fog if conditions are met
        if (!WeatherFogHandler.shouldApplyWeatherFog()) {
            return;
        }

        // Apply fog for both terrain and sky
        if (fogType == BackgroundRenderer.FogType.FOG_TERRAIN) {
            float fogStart = WeatherFogHandler.getFogStart();
            float fogEnd = WeatherFogHandler.getFogEnd();

            // Apply ground/terrain fog distances
            RenderSystem.setShaderFogStart(viewDistance * fogStart);
            RenderSystem.setShaderFogEnd(viewDistance * fogEnd);
        } else if (fogType == BackgroundRenderer.FogType.FOG_SKY) {
            float skyFogStart = WeatherFogHandler.getSkyFogStart();
            float skyFogEnd = WeatherFogHandler.getSkyFogEnd();

            // Apply sky fog distances
            RenderSystem.setShaderFogStart(viewDistance * skyFogStart);
            RenderSystem.setShaderFogEnd(viewDistance * skyFogEnd);
        }
    }

    /**
     * Inject to modify fog color for both terrain and sky
     */
    @Inject(
            method = "render",
            at = @At("RETURN")
    )
    private static void onRender(
            Camera camera,
            float tickDelta,
            net.minecraft.client.world.ClientWorld world,
            int viewDistance,
            float skyDarkness,
            CallbackInfo ci
    ) {
        if (!WeatherFogHandler.shouldApplyWeatherFog()) {
            return;
        }

        // Get fog color for terrain (will be applied to both)
        // Note: Minecraft uses same fog color for terrain and sky by default
        float[] fogColor = WeatherFogHandler.getFogColor(false);
        float red = RenderSystem.getShaderFogColor()[0] * fogColor[0];
        float green = RenderSystem.getShaderFogColor()[1] * fogColor[1];
        float blue = RenderSystem.getShaderFogColor()[2] * fogColor[2];
        float alpha = RenderSystem.getShaderFogColor()[3];

        RenderSystem.setShaderFogColor(red, green, blue, alpha);
    }

    /**
     * Additional mixin to modify sky color for cozy winter vibes
     */
    @Inject(
            method = "render",
            at = @At("HEAD")
    )
    private static void onRenderSky(
            Camera camera,
            float tickDelta,
            net.minecraft.client.world.ClientWorld world,
            int viewDistance,
            float skyDarkness,
            CallbackInfo ci
    ) {
        if (!WeatherFogHandler.shouldApplyWeatherFog()) {
            return;
        }

        // Apply sky-specific color tint
        float[] skyColor = WeatherFogHandler.getFogColor(true);
        float currentRed = RenderSystem.getShaderFogColor()[0];
        float currentGreen = RenderSystem.getShaderFogColor()[1];
        float currentBlue = RenderSystem.getShaderFogColor()[2];

        // Blend with sky color for atmospheric effect
        float blendedRed = currentRed * skyColor[0];
        float blendedGreen = currentGreen * skyColor[1];
        float blendedBlue = currentBlue * skyColor[2];

        RenderSystem.setShaderFogColor(blendedRed, blendedGreen, blendedBlue, 1.0f);
    }
}