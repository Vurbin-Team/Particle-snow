package cinematic.snowstorm;

import cinematic.snowstorm.config.SnowfallConfigManager;
import cinematic.snowstorm.particle.MyBreathParticle;
import cinematic.snowstorm.particle.MySnowflakeParticle;
import cinematic.snowstorm.particle.MyStreakParticle;
import cinematic.snowstorm.particle.ParticleTypes;
import cinematic.snowstorm.sounds.AdvancedWeatherSoundManager;
import cinematic.snowstorm.sounds.ModSounds;
import cinematic.snowstorm.utils.SnowSpawnManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;

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
		ParticleProviderRegistry.getInstance().register(ParticleTypes.MY_SNOWFLAKE, MySnowflakeParticle.Factory::new);
		ParticleProviderRegistry.getInstance().register(ParticleTypes.MY_SNOWFLAKE_REALISTIC, MySnowflakeParticle.Factory::new);
		ParticleProviderRegistry.getInstance().register(ParticleTypes.MY_SNOWSTREAK, MyStreakParticle.Factory::new);
		ParticleProviderRegistry.getInstance().register(ParticleTypes.MY_BREATH, MyBreathParticle.Factory::new);

		// Запуск менеджера спавна снега / тумана
		SnowSpawnManager.init();
	}
}