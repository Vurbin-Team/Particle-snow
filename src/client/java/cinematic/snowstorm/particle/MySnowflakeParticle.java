package cinematic.snowstorm.particle;

import cinematic.snowstorm.utils.SnowSpawnManager;
import cinematic.snowstorm.config.SnowfallConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.particle.BillboardParticle;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class MySnowflakeParticle extends BillboardParticle {
    // Physics constants
    private static final float GRAVITY_MULTIPLIER = 0.008f;
    private static final float FADE_START_RATIO = 0.90f;

    // Global wind direction (shared by all particles)
    private static float GLOBAL_WIND_ANGLE = 0.0f; // Direction in radians (0 = +X axis)

    // Individual snowflake properties
    private final float amplitudeX;
    private final float amplitudeZ;
    private final float seedX;
    private final float seedZ;
    private final float rotationSpeed;

    // Wind and drift
    private float windStrength;

    // Store initial alpha for fade calculations
    private final float initialAlpha;

    protected MySnowflakeParticle(ClientWorld world,
                                  double x, double y, double z,
                                  double vx, double vy, double vz,
                                  Sprite sprite) {
        super(world, x, y, z, vx, vy, vz, sprite);

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

        // Gravity strength
        this.gravityStrength = 0.40f + world.random.nextFloat() * 0.15f;

        // Using config values for size
        this.scale = SnowfallConfig.SIZE_MIN + world.random.nextFloat() * (SnowfallConfig.SIZE_MAX - SnowfallConfig.SIZE_MIN);

        // Initial velocity (from spawn manager) + fall speed from config
        this.velocityX = vx + (world.random.nextDouble() - 0.5) * 0.015;
        this.velocityZ = vz + (world.random.nextDouble() - 0.5) * 0.015;
        this.velocityY = -(SnowfallConfig.FALL_SPEED_MIN + world.random.nextDouble() * (SnowfallConfig.FALL_SPEED_MAX - SnowfallConfig.FALL_SPEED_MIN));

        // Using config values for alpha and color
        this.initialAlpha = SnowfallConfig.ALPHA_MIN + world.random.nextFloat() * (SnowfallConfig.ALPHA_MAX - SnowfallConfig.ALPHA_MIN);
        this.alpha = this.initialAlpha;

        // Set color to white for snow
        this.red = 1.0f;
        this.green = 1.0f;
        this.blue = 1.0f;

        // Set particle lifetime (15 sec = 300 ticks)
        this.maxAge = 300;

        // Ensure the particle collides with world
        this.collidesWithWorld = true;
    }

    @Override
    public void tick() {
        // Store previous position (for rendering interpolation)
        this.lastX = this.x;
        this.lastY = this.y;
        this.lastZ = this.z;

        // Store previous rotation for smooth interpolation
        this.lastZRotation = this.zRotation;

        // Age the particle
        if (this.age++ >= this.maxAge) {
            this.markDead();
            return;
        }

        // Fade out near end of life
        if (this.age > this.maxAge * FADE_START_RATIO) {
            float fadeProgress = (this.age - this.maxAge * FADE_START_RATIO) / (this.maxAge * (1.0f - FADE_START_RATIO));
            this.alpha = this.initialAlpha * (1.0f - fadeProgress);
        }

        // Apply gravity with variation
        this.velocityY -= GRAVITY_MULTIPLIER * this.gravityStrength;

        // Get player velocity for following behavior
        Vec3d playerVel = SnowSpawnManager.getPlayerVelocity();

        // Subtle following of player movement (prevents outrunning) - using config
        this.velocityX += playerVel.x * SnowfallConfig.PLAYER_FOLLOW_STRENGTH;
        this.velocityZ += playerVel.z * SnowfallConfig.PLAYER_FOLLOW_STRENGTH;

        // Apply unified wind force to VELOCITY (not position!)
        float windForceX = (float)Math.cos(GLOBAL_WIND_ANGLE) * windStrength * 0.002f;
        float windForceZ = (float)Math.sin(GLOBAL_WIND_ANGLE) * windStrength * 0.002f;
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
        float swayOffsetX = sinX * amplitudeX * 0.01f; // Scale down the sway for movement
        float swayOffsetZ = cosZ * amplitudeZ * 0.01f;

        // Apply movement with sway
        this.move(this.velocityX + swayOffsetX, this.velocityY, this.velocityZ + swayOffsetZ);

        // Check ground collision
        if (this.onGround) {
            this.markDead();
            return;
        }

        // Rotation for visual effect (using zRotation instead of angle)
        this.zRotation += rotationSpeed;
    }

    @Override
    protected RenderType getRenderType() {
        return RenderType.PARTICLE_ATLAS_TRANSLUCENT;
    }

    @Override
    protected int getBrightness(float tint) {
        // Full brightness for snow (maximum light level)
        // Light level format: sky light (upper 4 bits) | block light (lower 4 bits)
        // 15 << 20 = sky light at max, 15 << 4 = block light at max
        return 15728880; // 0xF000F0 in hex
    }

    /**
     * Optional: Method to change wind direction dynamically
     * @param angleInRadians Wind direction in radians (0 = +X axis)
     */
    public static void setWindDirection(float angleInRadians) {
        GLOBAL_WIND_ANGLE = angleInRadians;
    }

    /**
     * Get current wind direction
     * @return Wind direction in radians
     */
    public static float getWindDirection() {
        return GLOBAL_WIND_ANGLE;
    }

    /**
     * Factory for creating snowflake particles
     */
    @Environment(EnvType.CLIENT)
    public static class Factory implements ParticleFactory<SimpleParticleType> {
        private final SpriteProvider sprites;

        public Factory(SpriteProvider spriteProvider) {
            this.sprites = spriteProvider;
        }

        @Nullable
        @Override
        public Particle createParticle(SimpleParticleType parameters, ClientWorld world, double x, double y, double z, double vx, double vy, double vz, Random random) {
            // Get a random sprite from the sprite provider
            Sprite sprite = sprites.getSprite(random);

            return new MySnowflakeParticle(
                    world, x, y, z, vx, vy, vz, sprite
            );
        }
    }
}