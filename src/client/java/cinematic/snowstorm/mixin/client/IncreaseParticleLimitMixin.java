package cinematic.snowstorm.mixin.client;

import cinematic.snowstorm.config.SnowfallConfig;
import net.minecraft.client.particle.ParticleGroup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Increases the particle queue capacity for Minecraft 26.1.
 */
@Mixin(ParticleGroup.class)
public class IncreaseParticleLimitMixin {

    @ModifyConstant(method = "<init>", constant = @Constant(intValue = 16384))
    private int increaseParticleQueueCapacity(int defaultCapacity) {
        return Math.max(defaultCapacity, SnowfallConfig.MAX_PARTICLE_COUNT);
    }
}