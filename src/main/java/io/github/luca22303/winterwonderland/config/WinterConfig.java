package io.github.luca22303.winterwonderland.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.luca22303.winterwonderland.WinterWonderland;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * All settings of the mod, stored as JSON in {@code config/winterwonderland.json}.
 * Edited in-game through the Mod Menu settings screen.
 */
public class WinterConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("winterwonderland.json");

	private static WinterConfig instance = new WinterConfig();

	// --- World ---
	/** Master switch: makes the overworld wintry (graded by biome). */
	public boolean winterWorld = true;
	/** Maximum snow layers that pile up during snowfall (1-8). */
	public int maxSnowLayers = 3;
	/** Snow deeper than one layer slowly melts back on clear days. */
	public boolean meltOnClearDays = true;
	/** Lakes and rivers freeze over (oceans only along the shore). */
	public boolean freezeWater = true;
	/** Icicles grow under roof edges and overhangs. */
	public boolean icicles = true;

	// --- Visuals ---
	/** Grass and foliage shift to muted, frosty winter colors. Requires a chunk reload (F3+A) to apply. */
	public boolean wintryColors = true;
	/** Strength of the winter colors in percent. */
	public int wintryColorStrength = 60;
	public boolean snowflakes = true;
	/** Snowflake particle density in percent. */
	public int snowflakeDensity = 100;
	public boolean frostyBreath = true;
	public boolean winterFog = true;
	/** Fog strength in percent. */
	public int fogStrength = 100;
	/** Slightly warmer block light (lanterns, torches, fire). */
	public boolean warmGlow = true;
	/** Warm glow strength in percent. */
	public int warmGlowStrength = 50;

	// --- Weather ---
	/** Walking into blizzard wind is slower, with the wind at your back slightly faster. */
	public boolean windPush = true;
	/** Maximum slowdown against the wind in percent. */
	public int windPushStrength = 15;

	// --- Sounds ---
	public boolean windSounds = true;
	/** Wind volume in percent. */
	public int windVolume = 100;
	public boolean snowCrunch = true;
	public boolean fireCrackle = true;
	/** Outdoor weather sounds get quieter when you're inside. */
	public boolean muffleIndoors = true;

	public static WinterConfig get() {
		return instance;
	}

	public static void load() {
		if (Files.exists(PATH)) {
			try (Reader reader = Files.newBufferedReader(PATH)) {
				WinterConfig loaded = GSON.fromJson(reader, WinterConfig.class);
				if (loaded != null) {
					instance = loaded;
				}
			} catch (Exception e) {
				WinterWonderland.LOGGER.error("Could not read {}, using defaults", PATH, e);
			}
		}
		instance.clamp();
		save();
	}

	public static void save() {
		try {
			Files.createDirectories(PATH.getParent());
			try (Writer writer = Files.newBufferedWriter(PATH)) {
				GSON.toJson(instance, writer);
			}
		} catch (IOException e) {
			WinterWonderland.LOGGER.error("Could not write {}", PATH, e);
		}
	}

	private void clamp() {
		maxSnowLayers = clamp(maxSnowLayers, 1, 8);
		wintryColorStrength = clamp(wintryColorStrength, 0, 100);
		snowflakeDensity = clamp(snowflakeDensity, 0, 300);
		fogStrength = clamp(fogStrength, 0, 100);
		warmGlowStrength = clamp(warmGlowStrength, 0, 100);
		windPushStrength = clamp(windPushStrength, 0, 30);
		windVolume = clamp(windVolume, 0, 200);
	}

	private static int clamp(int value, int min, int max) {
		return Math.max(min, Math.min(max, value));
	}
}
