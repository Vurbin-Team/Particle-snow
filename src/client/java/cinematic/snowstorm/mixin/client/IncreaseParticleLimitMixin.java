package cinematic.snowstorm.mixin.client;

import cinematic.snowstorm.config.SnowfallConfig;
import net.minecraft.client.particle.ParticleGroup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Applies the configured per-render-layer particle limit in Minecraft 26.3.
 */
@Mixin(ParticleGroup.class)
public class IncreaseParticleLimitMixin {

    @ModifyConstant(method = "<init>", constant = @Constant(intValue = 16384))
    private int increaseParticleQueueCapacity(int defaultCapacity) {
        return particleLimit();
    }

    @ModifyConstant(
            method = "add(Lnet/minecraft/client/particle/Particle;)Z",
            constant = @Constant(intValue = 16384)
    )
    private int increaseMaximumParticleCount(int defaultLimit) {
        return particleLimit();
    }

    @ModifyConstant(
            method = "add(Lnet/minecraft/client/particle/Particle;)Z",
            constant = @Constant(intValue = 12288)
    )
    private int disableParticleReservoir(int defaultReservoirStart) {
        return particleLimit();
    }

    private static int particleLimit() {
        return Math.max(1, SnowfallConfig.MAX_PARTICLE_COUNT);
    }
}