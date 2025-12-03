package cinematic.snowstorm.particle;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ParticleTypes {
    // Создаем свой тип частицы
    public static final DefaultParticleType MY_SNOWFLAKE = FabricParticleTypes.simple();

    public static void register() {
        System.out.println("-------- Particles registered! --------");
        Registry.register(Registries.PARTICLE_TYPE,
                new Identifier("cinematic-snowstorm", "my_snowflake"),
                MY_SNOWFLAKE);
    }
}