package cinematic.snowstorm.mixin.client;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import net.minecraft.client.render.FrameGraphBuilder;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {

    @Inject(method = "renderWeather", at = @At("HEAD"), cancellable = true)
    private void disableWeatherRendering(FrameGraphBuilder frameGraphBuilder, Vec3d cameraPos, GpuBufferSlice fogBuffer, CallbackInfo ci) {
        // Отменяем рендеринг погоды
        ci.cancel();
    }
}