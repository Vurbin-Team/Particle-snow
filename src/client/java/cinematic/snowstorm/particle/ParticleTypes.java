package cinematic.snowstorm.particle;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ParticleTypes {
    // Создаем свой тип частицы
    public static final SimpleParticleType MY_SNOWFLAKE = FabricParticleTypes.simple();

    public static void register() {
        System.out.println("-------- Particles registered! --------");
        Registry.register(Registries.PARTICLE_TYPE,
                Identifier.of("cinematic-snowstorm", "my_snowflake"),
                MY_SNOWFLAKE);
    }
}