package cinematic.snowstorm.sounds;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.resources.Identifier;

public class ModSounds {

    public static final SoundEvent BLIZZARD_LIGHT = registerSound("weather.blizzard.light");
    public static final SoundEvent BLIZZARD_HEAVY = registerSound("weather.blizzard.heavy");

    private static SoundEvent registerSound(String name) {
        Identifier id = Identifier.fromNamespaceAndPath("cinematic-snowstorm", name);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
    }

    public static void initialize() {
        // Просто вызовите этот метод в вашем ModInitializer
    }
}