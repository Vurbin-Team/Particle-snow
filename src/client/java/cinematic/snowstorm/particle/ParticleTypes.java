package cinematic.snowstorm.particle;

import cinematic.snowstorm.config.SnowfallConfig;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ParticleTypes {
    // Stylized pixel-art snowflake (default look)
    public static final SimpleParticleType MY_SNOWFLAKE = FabricParticleTypes.simple(true);

    // Soft, photo-realistic snow particle (used when "Realistic Snow" is enabled)
    public static final SimpleParticleType MY_SNOWFLAKE_REALISTIC = FabricParticleTypes.simple(true);

    public static void register() {
        Registry.register(Registries.PARTICLE_TYPE,
                Identifier.of("cinematic-snowstorm", "my_snowflake"),
                MY_SNOWFLAKE);
        Registry.register(Registries.PARTICLE_TYPE,
                Identifier.of("cinematic-snowstorm", "my_snowflake_realistic"),
                MY_SNOWFLAKE_REALISTIC);
    }

    /**
     * Single source of truth for which snow particle type is currently active,
     * based on the "Realistic Snow" setting.
     */
    public static SimpleParticleType getActiveSnowType() {
        return SnowfallConfig.REALISTIC_SNOW ? MY_SNOWFLAKE_REALISTIC : MY_SNOWFLAKE;
    }
}