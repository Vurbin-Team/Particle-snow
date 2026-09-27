package cinematic.snowstorm.mixin.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class)
public class DisableRainDripParticlesMixin {

    @Inject(
            method = "addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void cancelRainDripParticle(
            ParticleOptions particle, double x, double y, double z,
            double velocityX, double velocityY, double velocityZ, CallbackInfo ci
    ) {
        if (particle == ParticleTypes.RAIN) {
            ci.cancel();
        }
    }
}
