package cinematic.snowstorm.mixin.client;

import cinematic.snowstorm.config.SnowfallConfig;
import com.google.common.collect.EvictingQueue;
import com.google.common.collect.Lists;
import net.minecraft.client.particle.EmitterParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.util.profiler.Profilers;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;

@Mixin(ParticleManager.class)
public class IncreaseParticleLimitMixin {

    @Shadow
    @Final
    private Map<ParticleTextureSheet, Queue<Particle>> particles;

    @Shadow
    @Final
    private Queue<EmitterParticle> newEmitterParticles;

    @Shadow
    @Final
    private Queue<Particle> newParticles;

    @Shadow
    private void tickParticles(java.util.Collection<Particle> particles) {}

    /**
     * @author CinematicSnowstorm
     * @reason Increase particle limit from 16384 to 64000
     */
    @Overwrite
    public void tick() {
        this.particles.forEach((sheet, queue) -> {
            Profilers.get().push(sheet.toString());
            this.tickParticles(queue);
            Profilers.get().pop();
        });

        if (!this.newEmitterParticles.isEmpty()) {
            List<EmitterParticle> list = Lists.newArrayList();
            Iterator<EmitterParticle> var2 = this.newEmitterParticles.iterator();

            while(var2.hasNext()) {
                EmitterParticle emitterParticle = var2.next();
                emitterParticle.tick();
                if (!emitterParticle.isAlive()) {
                    list.add(emitterParticle);
                }
            }

            this.newEmitterParticles.removeAll(list);
        }

        Particle particle;
        if (!this.newParticles.isEmpty()) {
            while((particle = this.newParticles.poll()) != null) {
                this.particles.computeIfAbsent(particle.getType(), (sheet) -> {
                    return EvictingQueue.create(SnowfallConfig.MAX_PARTICLE_COUNT); // Changed from 16384 to 64000
                }).add(particle);
            }
        }
    }
}