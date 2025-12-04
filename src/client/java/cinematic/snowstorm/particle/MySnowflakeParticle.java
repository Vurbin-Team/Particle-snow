package cinematic.snowstorm.particle;

import cinematic.snowstorm.utils.SnowSpawnManager;
import cinematic.snowstorm.config.SnowfallConfig;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.util.math.Vec3d;

public class MySnowflakeParticle extends SpriteBillboardParticle {
    // Physics constants
    private static final float GRAVITY_MULTIPLIER = 0.008f;
    private static final float FADE_START_RATIO = 0.90f;

    // Individual snowflake properties
    private final float amplitudeX;
    private final float amplitudeZ;
    private final float seedX;
    private final float seedZ;
    private final float rotationSpeed;

    // Wind and drift
    private float windStrength;

    protected MySnowflakeParticle(ClientWorld world,
                                  double x, double y, double z,
                                  double vx, double vy, double vz,
                                  SpriteProvider spriteProvider) {
        super(world, x, y, z, vx, vy, vz);

        // Unique oscillation patterns for each snowflake - using config values
        this.amplitudeX = SnowfallConfig.SWAY_AMOUNT_MIN +
                world.random.nextFloat() * (SnowfallConfig.SWAY_AMOUNT_MAX - SnowfallConfig.SWAY_AMOUNT_MIN);
        this.amplitudeZ = SnowfallConfig.SWAY_AMOUNT_MIN +
                world.random.nextFloat() * (SnowfallConfig.SWAY_AMOUNT_MAX - SnowfallConfig.SWAY_AMOUNT_MIN) * 0.7f;
        this.seedX = (float)(world.random.nextDouble() * Math.PI * 2);
        this.seedZ = (float)(world.random.nextDouble() * Math.PI * 2);

        // Rotation for visual variety - using config
        this.rotationSpeed = (float)(world.random.nextDouble() - 0.1f) * SnowfallConfig.ROTATION_SPEED;

        // Wind variation - using config (strength varies, but direction is shared)
        this.windStrength = SnowfallConfig.WIND_MIN + world.random.nextFloat() * (SnowfallConfig.WIND_MAX - SnowfallConfig.WIND_MIN);

        this.setSpriteForAge(spriteProvider);
        this.gravityStrength = 0.40f + world.random.nextFloat() * 0.15f;

        // Using config values for size
        this.scale = SnowfallConfig.SIZE_MIN + world.random.nextFloat() * (SnowfallConfig.SIZE_MAX - SnowfallConfig.SIZE_MIN);

        // Initial velocity (from spawn manager) + fall speed from config
        this.velocityX = vx + (world.random.nextDouble() - 0.5) * 0.015;
        this.velocityZ = vz + (world.random.nextDouble() - 0.5) * 0.015;
        this.velocityY = -(SnowfallConfig.FALL_SPEED_MIN + world.random.nextDouble() * (SnowfallConfig.FALL_SPEED_MAX - SnowfallConfig.FALL_SPEED_MIN));

        // Using config values for alpha
        this.alpha = SnowfallConfig.ALPHA_MIN + world.random.nextFloat() * (SnowfallConfig.ALPHA_MAX - SnowfallConfig.ALPHA_MIN);

        // Set particle lifetime (15 sec)
        this.maxAge = 300;
    }

    @Override
    public void tick() {
        // Store previous position
        this.prevPosX = this.x;
        this.prevPosY = this.y;
        this.prevPosZ = this.z;

        // Age the particle
        if (this.age++ >= this.maxAge) {
            this.markDead();
            return;
        }

        // Fade out near end of life
        if (this.age > this.maxAge * FADE_START_RATIO) {
            float fadeProgress = (this.age - this.maxAge * FADE_START_RATIO) / (this.maxAge * (1.0f - FADE_START_RATIO));
            this.alpha = (SnowfallConfig.ALPHA_MIN + (SnowfallConfig.ALPHA_MAX - SnowfallConfig.ALPHA_MIN) * 0.5f) * (1.0f - fadeProgress);
        }

        // Apply gravity with variation
        this.velocityY -= GRAVITY_MULTIPLIER * this.gravityStrength;

        // Get player velocity for following behavior
        Vec3d playerVel = SnowSpawnManager.getPlayerVelocity();

        // Subtle following of player movement (prevents outrunning) - using config
        this.velocityX += playerVel.x * SnowfallConfig.PLAYER_FOLLOW_STRENGTH;
        this.velocityZ += playerVel.z * SnowfallConfig.PLAYER_FOLLOW_STRENGTH;

        // Apply unified wind force to VELOCITY (not position!)
        float windForceX = (float)Math.cos(SnowfallConfig.GLOBAL_WIND_ANGLE_X) * windStrength * 0.002f;
        float windForceZ = (float)Math.sin(SnowfallConfig.GLOBAL_WIND_ANGLE_X) * windStrength * 0.002f;
        this.velocityX += windForceX;
        this.velocityZ += windForceZ;

        // Apply drag (air resistance) - using config
        this.velocityX *= SnowfallConfig.AIR_DRAG;
        this.velocityZ *= SnowfallConfig.AIR_DRAG;

        // Natural snowflake movement - swaying and drifting - using config
        float t = this.age * SnowfallConfig.SWAY_SPEED;

        // Independent X and Z oscillations for more natural movement
        float sinX = (float)Math.sin(t + seedX);
        float cosZ = (float)Math.cos(t * 1.2f + seedZ);

        // Apply oscillations directly to position (this is fine for sway)
        float swayOffsetX = sinX * amplitudeX;
        float swayOffsetZ = cosZ * amplitudeZ;

        // Apply movement with sway
        this.move(this.velocityX + swayOffsetX, this.velocityY, this.velocityZ + swayOffsetZ);

        // Check ground collision
        if (this.onGround) {
            this.markDead();
            return;
        }

        // Rotation for visual effect
        this.angle += rotationSpeed;
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public int getBrightness(float tint) {
        // Full brightness for snow
        return 15728880;
    }

    public static class Factory implements net.minecraft.client.particle.ParticleFactory<DefaultParticleType> {
        private final SpriteProvider sprites;

        public Factory(SpriteProvider spriteProvider) {
            this.sprites = spriteProvider;
        }

        @Override
        public Particle createParticle(DefaultParticleType type,
                                       ClientWorld world,
                                       double x, double y, double z,
                                       double vx, double vy, double vz) {
            MySnowflakeParticle particle = new MySnowflakeParticle(
                    world, x, y, z, vx, vy, vz, sprites
            );
            particle.setSprite(sprites.getSprite(world.random));
            return particle;
        }
    }
}