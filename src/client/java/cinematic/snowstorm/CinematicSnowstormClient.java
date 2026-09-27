package cinematic.snowstorm;

import cinematic.snowstorm.config.SnowfallConfigManager;
import cinematic.snowstorm.particle.MySnowflakeParticle;
import cinematic.snowstorm.particle.ParticleTypes;
import cinematic.snowstorm.sounds.AdvancedWeatherSoundManager;
import cinematic.snowstorm.sounds.ModSounds;
import cinematic.snowstorm.utils.SnowSpawnManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;

public class CinematicSnowstormClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		ModSounds.initialize();

		AdvancedWeatherSoundManager.init();

		SnowfallConfigManager.load();

		// Регистрируем типы частиц
		ParticleTypes.register();

		// Both snow particle types share the same behaviour/physics -
		// only their sprite textures (defined in the particle JSON files) differ.
		ParticleFactoryRegistry.getInstance().register(ParticleTypes.MY_SNOWFLAKE, MySnowflakeParticle.Factory::new);
		ParticleFactoryRegistry.getInstance().register(ParticleTypes.MY_SNOWFLAKE_REALISTIC, MySnowflakeParticle.Factory::new);

		// Запуск менеджера спавна снега / тумана
		SnowSpawnManager.init();
	}
}