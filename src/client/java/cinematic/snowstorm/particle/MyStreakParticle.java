package cinematic.snowstorm.particle;

import cinematic.snowstorm.config.SnowfallConfig;
import cinematic.snowstorm.effects.SpeedStreaks;
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
 * A short-lived, stationary streak (see {@link SpeedStreaks}). Its sprite is a thin vertical
 * line that is rotated every tick to follow the on-screen direction of motion.
 */
@Environment(EnvType.CLIENT)
public class MyStreakParticle extends SingleQuadParticle {
    // Ticks of age added per tick once the player is past the streak (1 = normal speed).
    private static final int BEHIND_AGE_STEP = 2;

    private final float baseAlpha;

    protected MyStreakParticle(ClientLevel world, double x, double y, double z, TextureAtlasSprite sprite) {
        super(world, x, y, z, 0.0, 0.0, 0.0, sprite);
        this.xd = 0.0;
        this.yd = 0.0;
        this.zd = 0.0;
        this.hasPhysics = false;
        this.lifetime = 6 + this.random.nextInt(5);
        this.quadSize = (0.7f + this.random.nextFloat() * 0.6f) * Math.max(0.1f, SnowfallConfig.STREAK_LENGTH);
        this.baseAlpha = 0.35f + this.random.nextFloat() * 0.20f;
        this.alpha = 0.0f;
        this.rCol = 1.0f;
        this.gCol = 1.0f;
        this.bCol = 1.0f;
        this.roll = SpeedStreaks.streakRoll(x, y, z);
        this.oRoll = this.roll;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        this.oRoll = this.roll;

        if (this.age >= this.lifetime) {
            this.remove();
            return;
        }
        // Streaks the player has already flown past age twice as fast, so they vanish sooner.
        this.age += SpeedStreaks.isBehind(this.x, this.y, this.z) ? BEHIND_AGE_STEP : 1;
        float t = Math.min(1.0f, (float) this.age / this.lifetime);

        // Fade in and out over the short lifetime; invisible in the cleared middle of the screen.
        this.alpha = this.baseAlpha * (float) Math.sin(Math.PI * t)
                * SpeedStreaks.centerFade(this.x, this.y, this.z);
        this.roll = SpeedStreaks.streakRoll(this.x, this.y, this.z);
    }

    @Override
    protected Layer getLayer() {
        return Layer.TRANSLUCENT;
    }

    @Override
    protected int getLightCoords(float tint) {
        return 15728880; // full brightness, like the snowflakes
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
            return new MyStreakParticle(world, x, y, z, sprites.get(random));
        }
    }
}
