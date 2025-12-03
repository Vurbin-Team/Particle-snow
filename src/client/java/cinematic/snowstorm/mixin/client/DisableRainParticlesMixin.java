package cinematic.snowstorm.mixin.client;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to disable vanilla rain drip particles on the ground
 */
@Mixin(ClientWorld.class)
public class DisableRainParticlesMixin {

    /**
     * Cancel rain splash particles that appear when rain hits the ground
     */
    @Inject(
            method = "addParticle(DDDDDLnet/minecraft/particle/ParticleEffect;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void disableRainDrips(double minX, double maxX, double minZ, double maxZ, double y, ParticleEffect parameters, CallbackInfo ci) {
        // Cancel rain splash and drip particles
        if (parameters.getType() == ParticleTypes.RAIN ||
                parameters.getType() == ParticleTypes.DRIPPING_WATER) {
            ci.cancel();
        }
    }
}