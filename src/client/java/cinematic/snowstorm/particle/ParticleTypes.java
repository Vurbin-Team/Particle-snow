package cinematic.snowstorm.particle;

import cinematic.snowstorm.config.SnowfallConfig;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.core.Registry;

public class ParticleTypes {
    // Stylized pixel-art snowflake (default look)
    public static final SimpleParticleType MY_SNOWFLAKE = FabricParticleTypes.simple(true);

    // Soft, photo-realistic snow particle (used when "Realistic Snow" is enabled)
    public static final SimpleParticleType MY_SNOWFLAKE_REALISTIC = FabricParticleTypes.simple(true);

    // Speed streaks and frost breath (see the effects package)
    public static final SimpleParticleType MY_SNOWSTREAK = FabricParticleTypes.simple(true);
    public static final SimpleParticleType MY_BREATH = FabricParticleTypes.simple(true);

    public static void register() {
        Registry.register(BuiltInRegistries.PARTICLE_TYPE,
                Identifier.fromNamespaceAndPath("cinematic-snowstorm", "my_snowflake"),
                MY_SNOWFLAKE);
        Registry.register(BuiltInRegistries.PARTICLE_TYPE,
                Identifier.fromNamespaceAndPath("cinematic-snowstorm", "my_snowflake_realistic"),
                MY_SNOWFLAKE_REALISTIC);
        Registry.register(BuiltInRegistries.PARTICLE_TYPE,
                Identifier.fromNamespaceAndPath("cinematic-snowstorm", "my_snowstreak"),
                MY_SNOWSTREAK);
        Registry.register(BuiltInRegistries.PARTICLE_TYPE,
                Identifier.fromNamespaceAndPath("cinematic-snowstorm", "my_breath"),
                MY_BREATH);
    }

    /**
     * Single source of truth for which snow particle type is currently active,
     * based on the "Realistic Snow" setting.
     */
    public static SimpleParticleType getActiveSnowType() {
        return SnowfallConfig.REALISTIC_SNOW ? MY_SNOWFLAKE_REALISTIC : MY_SNOWFLAKE;
    }
}