package cinematic.snowstorm.mixin.client;

import cinematic.snowstorm.config.SnowfallConfig;
import com.google.common.collect.EvictingQueue;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Queue;

/**
 * Mixin to increase particle limit by modifying ParticleRenderer's queue capacity
 * for Minecraft 1.21.6
 */
@Mixin(ParticleRenderer.class)
public class IncreaseParticleLimitMixin {

    @Shadow
    @Final
    @Mutable
    private Queue<Particle> particles;

    /**
     * Inject into constructor to replace the default EvictingQueue
     * with one that has a larger capacity
     */
    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        // Заменяем очередь на новую с увеличенным лимитом
        this.particles = EvictingQueue.create(SnowfallConfig.MAX_PARTICLE_COUNT);
    }
}