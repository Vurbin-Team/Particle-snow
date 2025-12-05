package cinematic.snowstorm.sounds;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class ModSounds {

    public static final SoundEvent BLIZZARD_LIGHT = registerSound("weather.blizzard.light");
    public static final SoundEvent BLIZZARD_HEAVY = registerSound("weather.blizzard.heavy");

    private static SoundEvent registerSound(String name) {
        Identifier id = new Identifier("cinematic-snowstorm", name);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }

    public static void initialize() {
        // Просто вызовите этот метод в вашем ModInitializer
    }
}