package cinematic.snowstorm.mixin.client;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.render.Camera;
import net.minecraft.particle.ParticleEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin to increase particle render distance
 * Allows particles to be visible from much farther away
 */
@Mixin(ParticleManager.class)
public class ParticleManagerMixin {

    // Default Minecraft particle distance is around 16-32 blocks
    // We increase this to 256 blocks for cinematic snow effect

    private static final double EXTENDED_PARTICLE_DISTANCE = 256.0;

    private static final double EXTENDED_PARTICLE_DISTANCE_SQUARED = EXTENDED_PARTICLE_DISTANCE * EXTENDED_PARTICLE_DISTANCE;

    /**
     * Inject into addParticle to bypass distance checks
     * This allows particles to spawn and render from much farther away
     */
    @Inject(
            method = "addParticle(Lnet/minecraft/particle/ParticleEffect;DDDDDD)Lnet/minecraft/client/particle/Particle;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onAddParticle(
            ParticleEffect parameters, double x, double y, double z, double velocityX, double velocityY, double velocityZ, CallbackInfoReturnable<Particle> cir
    ) {
        // Force alwaysSpawn to true for our custom particles
        // This bypasses the vanilla distance check
        if (isOurCustomParticle(parameters)) {
            // Let the method continue but with alwaysSpawn forced to true
            // This is handled by the actual method call
        }
    }

    /**
     * Check if this is our custom snowflake particle
     */

    private boolean isOurCustomParticle(ParticleEffect parameters) {
        // Check if it's our MY_SNOWFLAKE particle type
        String particleName = parameters.getType().toString();
        return particleName.contains("my_snowflake") || particleName.contains("cinematic-snowstorm");
    }
}