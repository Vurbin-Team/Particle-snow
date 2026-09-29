package cinematic.snowstorm.particle;

import cinematic.snowstorm.utils.SnowSpawnManager;
import cinematic.snowstorm.config.SnowfallConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class MySnowflakeParticle extends SingleQuadParticle {
    private static final double COLLISION_EPSILON = 1.0E-5;
    private static final float FADE_START_RATIO = 0.90f;

    // Individual snowflake properties
    private final float amplitudeX;
    private final float amplitudeZ;
    private final float seedX;
    private final float seedZ;
    private final float rotationSpeed;
    private final float fallSpeed;

    // Store initial alpha for fade calculations
    private final float initialAlpha;

    protected MySnowflakeParticle(ClientLevel world,
                                  double x, double y, double z,
                                  double vx, double vy, double vz,
                                  TextureAtlasSprite sprite) {
        super(world, x, y, z, vx, vy, vz, sprite);

        // Unique oscillation patterns for each snowflake - using config values
        this.amplitudeX = SnowfallConfig.SWAY_AMOUNT_MIN +
                this.random.nextFloat() * (SnowfallConfig.SWAY_AMOUNT_MAX - SnowfallConfig.SWAY_AMOUNT_MIN);
        this.amplitudeZ = SnowfallConfig.SWAY_AMOUNT_MIN +
                this.random.nextFloat() * (SnowfallConfig.SWAY_AMOUNT_MAX - SnowfallConfig.SWAY_AMOUNT_MIN) * 0.7f;
        this.seedX = (float)(this.random.nextDouble() * Math.PI * 2);
        this.seedZ = (float)(this.random.nextDouble() * Math.PI * 2);

        // Rotation for visual variety - using config
        this.rotationSpeed = (float)(this.random.nextDouble() - 0.1f) * SnowfallConfig.ROTATION_SPEED;

        // Using config values for size
        this.quadSize = SnowfallConfig.SIZE_MIN + this.random.nextFloat() * (SnowfallConfig.SIZE_MAX - SnowfallConfig.SIZE_MIN);

        // Initial velocity (from spawn manager) + fall speed from config
        double windAngle = Math.toRadians(SnowfallConfig.WIND_ANGLE);
        float windStrength = Math.max(0.0f, SnowfallConfig.WIND_STRENGTH);
        this.xd = vx + Math.cos(windAngle) * windStrength
                + (this.random.nextDouble() - 0.5) * 0.015;
        this.zd = vz + Math.sin(windAngle) * windStrength
                + (this.random.nextDouble() - 0.5) * 0.015;
        float minFallSpeed = Math.max(0.0f, Math.min(SnowfallConfig.FALL_SPEED_MIN, SnowfallConfig.FALL_SPEED_MAX));
        float maxFallSpeed = Math.max(0.0f, Math.max(SnowfallConfig.FALL_SPEED_MIN, SnowfallConfig.FALL_SPEED_MAX));
        this.fallSpeed = minFallSpeed + this.random.nextFloat() * (maxFallSpeed - minFallSpeed);
        this.yd = -this.fallSpeed;

        // Using config values for alpha and color
        this.initialAlpha = SnowfallConfig.ALPHA_MIN + this.random.nextFloat() * (SnowfallConfig.ALPHA_MAX - SnowfallConfig.ALPHA_MIN);
        this.alpha = this.initialAlpha;

        // Set color to white for snow
        this.rCol = 1.0f;
        this.gCol = 1.0f;
        this.bCol = 1.0f;

        // Set particle lifetime (30 sec = 600 ticks)
        this.lifetime = 600;

        // Ensure the particle collides with world
        this.hasPhysics = true;
    }

    @Override
    public void tick() {
        // Store previous position (for rendering interpolation)
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        // Store previous rotation for smooth interpolation
        this.oRoll = this.roll;

        // Age the particle
        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }

        // Fade out near end of life
        if (this.age > this.lifetime * FADE_START_RATIO) {
            float fadeProgress = (this.age - this.lifetime * FADE_START_RATIO) / (this.lifetime * (1.0f - FADE_START_RATIO));
            this.alpha = this.initialAlpha * (1.0f - fadeProgress);
        }

        // Keep the configured fall speed constant throughout the particle's lifetime.
        this.yd = -this.fallSpeed;

        // Get player velocity for following behavior
        Vec3 playerVel = SnowSpawnManager.getPlayerVelocity();

        // Natural snowflake movement - swaying and drifting - using config
        float t = this.age * SnowfallConfig.SWAY_SPEED;

        // Independent X and Z oscillations for more natural movement
        float sinX = (float)Math.sin(t + seedX);
        float cosZ = (float)Math.cos(t * 1.2f + seedZ);

        // Apply oscillations directly to position (this is fine for sway)
        float swayOffsetX = sinX * amplitudeX * 0.01f; // Scale down the sway for movement
        float swayOffsetZ = cosZ * amplitudeZ * 0.01f;

        // Remove the particle if collision resolution blocks movement on any axis.
        double moveX = this.xd + playerVel.x * SnowfallConfig.PLAYER_FOLLOW_STRENGTH + swayOffsetX;
        double moveY = this.yd;
        double moveZ = this.zd + playerVel.z * SnowfallConfig.PLAYER_FOLLOW_STRENGTH + swayOffsetZ;
        double expectedX = this.x + moveX;
        double expectedY = this.y + moveY;
        double expectedZ = this.z + moveZ;
        this.move(moveX, moveY, moveZ);

        if (this.onGround
                || Math.abs(this.x - expectedX) > COLLISION_EPSILON
                || Math.abs(this.y - expectedY) > COLLISION_EPSILON
                || Math.abs(this.z - expectedZ) > COLLISION_EPSILON) {
            this.remove();
            return;
        }

        // Rotation for visual effect (using zRotation instead of angle)
        this.roll += rotationSpeed;
    }

    @Override
    protected Layer getLayer() {
        return Layer.TRANSLUCENT;
    }

    @Override
    protected int getLightCoords(float tint) {
        // Full brightness for snow (maximum light level)
        // Light level format: sky light (upper 4 bits) | block light (lower 4 bits)
        // 15 << 20 = sky light at max, 15 << 4 = block light at max
        return 15728880; // 0xF000F0 in hex
    }

    public static Vec3 getExpectedWindDrift(int fallTicks) {
        if (fallTicks <= 0) {
            return Vec3.ZERO;
        }

        double angle = Math.toRadians(SnowfallConfig.WIND_ANGLE);
        double horizontalOffset = Math.max(0.0f, SnowfallConfig.WIND_STRENGTH) * fallTicks;

        return new Vec3(
                Math.cos(angle) * horizontalOffset,
                0.0,
                Math.sin(angle) * horizontalOffset
        );
    }

    /**
     * Factory for creating snowflake particles
     */
    @Environment(EnvType.CLIENT)
    public static class Factory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Factory(SpriteSet spriteProvider) {
            this.sprites = spriteProvider;
        }

        @Nullable
        @Override
        public Particle createParticle(SimpleParticleType parameters, ClientLevel world, double x, double y, double z, double vx, double vy, double vz, RandomSource random) {
            // Get a random sprite from the sprite provider
            TextureAtlasSprite sprite = sprites.get(random);

            return new MySnowflakeParticle(
                    world, x, y, z, vx, vy, vz, sprite
            );
        }
    }
}