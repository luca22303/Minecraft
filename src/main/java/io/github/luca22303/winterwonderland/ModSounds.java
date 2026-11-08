package io.github.luca22303.winterwonderland;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

/**
 * Sound events of the mod. Which files they play is defined in
 * {@code assets/winterwonderland/sounds.json} - currently vanilla sounds, so custom .ogg files can be
 * swapped in later without touching code.
 */
public final class ModSounds {
	/** Looping wind during snowfall and blizzards. */
	public static final SoundEvent WIND_LOOP = register("ambient.wind_loop");
	/** Occasional wind gusts in blizzards. */
	public static final SoundEvent WIND_GUST = register("ambient.wind_gust");
	/** Extra crunch layered on footsteps in snow. */
	public static final SoundEvent SNOW_CRUNCH = register("block.snow.crunch");
	/** Cozy crackling near fires, campfires and lit furnaces. */
	public static final SoundEvent FIRE_CRACKLE = register("ambient.fire_crackle");

	private ModSounds() {
	}

	private static SoundEvent register(String name) {
		Identifier id = WinterWonderland.id(name);
		return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
	}

	public static void init() {
		// Loads the class, which registers the sounds.
	}
}
