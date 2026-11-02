package cinematic.snowstorm.effects;

import cinematic.snowstorm.config.SnowfallConfig;
import cinematic.snowstorm.particle.ParticleTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;

/**
 * Frost breath: small puffs of vapor at the player's mouth while it is snowing and the player
 * is outside (open sky, not underwater). Only the local player breathes.
 */
public final class FrostBreath {
    private static final int PUFFS_PER_EXHALE = 3;
    // Exhale speed in blocks per tick, relative to the player.
    private static final double EXHALE_SPEED = 0.03;
    // Share of the player's own motion the vapor starts with (1.0 = moves along with the player).
    private static final double MOTION_INHERIT = 1.0;

    private static int cooldown;
    private static int puffsLeft;

    private FrostBreath() {}

    public static void tick(ClientLevel level, LocalPlayer player, boolean snowing) {
        if (!SnowfallConfig.ENABLE_FROST_BREATH || !snowing || player == null
                || player.isSpectator() || !player.isAlive() || player.isUnderWater()
                || !level.canSeeSky(player.blockPosition())) {
            puffsLeft = 0;
            return;
        }
        if (!SnowfallConfig.BREATH_FIRST_PERSON
                && Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
            puffsLeft = 0;
            return;
        }

        if (puffsLeft > 0) {
            puffsLeft--;
            puff(level, player);
            return;
        }
        if (--cooldown <= 0) {
            double intervalTicks = Math.max(0.5, SnowfallConfig.BREATH_INTERVAL_SECONDS) * 20.0;
            cooldown = (int) Math.max(5, intervalTicks * (0.8 + level.getRandom().nextDouble() * 0.4));
            puffsLeft = PUFFS_PER_EXHALE - 1;
            puff(level, player);
        }
    }

    private static void puff(ClientLevel level, LocalPlayer player) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        // Real movement during the last tick (not the smoothed value used for snow), so the
        // vapor follows the actual walking direction: forward, sideways, backward, jumping.
        double mx = player.getX() - player.xo;
        double my = player.getY() - player.yo;
        double mz = player.getZ() - player.zo;
        // The exhaled air starts with the body's motion plus the exhale itself, then drag and
        // wind take over in MyBreathParticle (so the puff trails behind a walking player).
        level.addParticle(ParticleTypes.MY_BREATH,
                eye.x + look.x * 0.32,
                eye.y - 0.22 + look.y * 0.10,
                eye.z + look.z * 0.32,
                look.x * EXHALE_SPEED + mx * MOTION_INHERIT,
                look.y * EXHALE_SPEED + 0.006 + my * MOTION_INHERIT,
                look.z * EXHALE_SPEED + mz * MOTION_INHERIT);
    }
}
