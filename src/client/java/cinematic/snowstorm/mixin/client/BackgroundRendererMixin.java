package cinematic.snowstorm.mixin.client;

import cinematic.snowstorm.fog.WeatherFogHandler;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Fog;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin to modify fog rendering in BackgroundRenderer for Minecraft 1.21.2+
 */
@Mixin(BackgroundRenderer.class)
public class BackgroundRendererMixin {

    /**
     * Inject into applyFog to modify fog distances and colors
     * In 1.21.2+, applyFog returns Fog object and includes color parameter
     */
    @Inject(
            method = "applyFog",
            at = @At("RETURN"),
            cancellable = true
    )
    private static void onApplyFog(
            Camera camera,
            BackgroundRenderer.FogType fogType,
            Vector4f color,
            float viewDistance,
            boolean thickenFog,
            float tickDelta,
            CallbackInfoReturnable<Fog> cir
    ) {
        // Update fog handler
        WeatherFogHandler.updateFog();

        // Only apply weather fog if conditions are met
        if (!WeatherFogHandler.shouldApplyWeatherFog()) {
            return;
        }

        // Get the returned Fog object
        Fog originalFog = cir.getReturnValue();
        if (originalFog == null) return;

        // Calculate new fog distances based on fog type
        float fogStart;
        float fogEnd;

        if (fogType == BackgroundRenderer.FogType.FOG_TERRAIN) {
            fogStart = WeatherFogHandler.getFogStart();
            fogEnd = WeatherFogHandler.getFogEnd();
        } else if (fogType == BackgroundRenderer.FogType.FOG_SKY) {
            fogStart = WeatherFogHandler.getSkyFogStart();
            fogEnd = WeatherFogHandler.getSkyFogEnd();
        } else {
            return;
        }

        // Get fog color modifier
        float[] fogColor = WeatherFogHandler.getFogColor(fogType == BackgroundRenderer.FogType.FOG_SKY);

        // Apply color modification to existing colors
        float red = originalFog.red() * fogColor[0];
        float green = originalFog.green() * fogColor[1];
        float blue = originalFog.blue() * fogColor[2];

        // Create new Fog with modified distances and colors
        Fog modifiedFog = new Fog(
                viewDistance * fogStart,  // start
                viewDistance * fogEnd,    // end
                originalFog.shape(),      // shape (SPHERE, CYLINDER, etc.)
                red,                      // red component
                green,                    // green component
                blue,                     // blue component
                originalFog.alpha()       // alpha component
        );

        // Set the modified fog as return value
        cir.setReturnValue(modifiedFog);
    }

    /**
     * Inject into getFogColor to modify fog color
     * This is called before applyFog and determines the base fog color
     */
    @Inject(
            method = "getFogColor",
            at = @At("RETURN"),
            cancellable = true
    )
    private static void onGetFogColor(
            Camera camera,
            float tickDelta,
            net.minecraft.client.world.ClientWorld world,
            int clampedViewDistance,
            float skyDarkness,
            CallbackInfoReturnable<Vector4f> cir
    ) {
        if (!WeatherFogHandler.shouldApplyWeatherFog()) {
            return;
        }

        // Get original color
        Vector4f originalColor = cir.getReturnValue();
        if (originalColor == null) return;

        // Get weather fog color modifier (not sky-specific at this stage)
        float[] fogColor = WeatherFogHandler.getFogColor(false);

        // Apply color modification
        Vector4f modifiedColor = new Vector4f(
                originalColor.x * fogColor[0],
                originalColor.y * fogColor[1],
                originalColor.z * fogColor[2],
                originalColor.w
        );

        cir.setReturnValue(modifiedColor);
    }
}