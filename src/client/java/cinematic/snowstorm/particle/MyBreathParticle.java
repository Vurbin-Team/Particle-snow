package cinematic.snowstorm.particle;

import cinematic.snowstorm.config.SnowfallConfig;
import cinematic.snowstorm.effects.WindGusts;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;

/**
 * A puff of frost breath: grows, drifts with the wind and fades out.
 */
@Environment(EnvType.CLIENT)
public class MyBreathParticle extends SingleQuadParticle {
    private final float startSize;
    private final float endSize;
    private final float maxAlpha;
    private final float rollSpeed;

    protected MyBreathParticle(ClientLevel world, double x, double y, double z,
                               double vx, double vy, double vz, TextureAtlasSprite sprite) {
        super(world, x, y, z, vx, vy, vz, sprite);
        float intensity = Math.max(0.1f, SnowfallConfig.BREATH_INTENSITY); // opacity
        float size = Math.max(0.1f, SnowfallConfig.BREATH_SIZE);
        this.xd = vx;
        this.yd = vy;
        this.zd = vz;
        this.hasPhysics = false;
        this.lifetime = 28 + this.random.nextInt(13);
        this.startSize = 0.12f * size;
        this.endSize = (0.45f + this.random.nextFloat() * 0.12f) * size;
        this.quadSize = this.startSize;
        this.maxAlpha = Math.min(0.9f, 0.38f * intensity);
        this.alpha = 0.0f;
        this.rollSpeed = (this.random.nextFloat() - 0.5f) * 0.04f;
        this.rCol = 0.92f;
        this.gCol = 0.96f;
        this.bCol = 1.0f;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        this.oRoll = this.roll;

        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }
        float p = (float) this.age / this.lifetime;
        // Ease-out growth; quick fade-in, slow fade-out.
        this.quadSize = this.startSize + (this.endSize - this.startSize) * (1.0f - (1.0f - p) * (1.0f - p));
        this.alpha = this.maxAlpha * (p < 0.15f ? p / 0.15f : 1.0f - (p - 0.15f) / 0.85f);

        // Slow down and drift downwind (base wind + current gust).
        double windAngle = Math.toRadians(SnowfallConfig.WIND_ANGLE);
        double wind = Math.max(0.0f, SnowfallConfig.WIND_STRENGTH) * 0.08;
        this.xd = this.xd * 0.90 + Math.cos(windAngle) * wind + WindGusts.getGustX() * 0.2;
        this.yd = this.yd * 0.94;
        this.zd = this.zd * 0.90 + Math.sin(windAngle) * wind + WindGusts.getGustZ() * 0.2;
        this.move(this.xd, this.yd, this.zd);
        this.roll += this.rollSpeed;
    }

    @Override
    protected Layer getLayer() {
        return Layer.TRANSLUCENT;
    }

    @Override
    protected int getLightCoords(float tint) {
        return 15728880;
    }

    @Environment(EnvType.CLIENT)
    public static class Factory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Factory(SpriteSet spriteProvider) {
            this.sprites = spriteProvider;
        }

        @Nullable
        @Override
        public Particle createParticle(SimpleParticleType parameters, ClientLevel world, double x, double y, double z, double vx, double vy, double vz, RandomSource random) {
            return new MyBreathParticle(world, x, y, z, vx, vy, vz, sprites.get(random));
        }
    }
}
