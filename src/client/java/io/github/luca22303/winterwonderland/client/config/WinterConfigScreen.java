package io.github.luca22303.winterwonderland.client.config;

import io.github.luca22303.winterwonderland.config.WinterConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/** In-game settings screen (opened from Mod Menu). */
public class WinterConfigScreen extends OptionsSubScreen {
	private static final String PREFIX = "winterwonderland.config.";

	private final WinterConfig config = WinterConfig.get();
	private final List<Runnable> appliers = new ArrayList<>();
	private boolean colorsBefore;
	private int colorStrengthBefore;
	private boolean winterBefore;
	private boolean leafTopsBefore;

	public WinterConfigScreen(Screen parent) {
		super(parent, Minecraft.getInstance().options, Component.translatable(PREFIX + "title"));
	}

	@Override
	protected void addOptions() {
		colorsBefore = config.wintryColors;
		colorStrengthBefore = config.wintryColorStrength;
		winterBefore = config.winterWorld;
		leafTopsBefore = config.snowyLeafTops;
		appliers.clear();

		this.list.addHeader(Component.translatable(PREFIX + "section.world"));
		this.list.addSmall(
			toggle("winter_world", config.winterWorld, v -> config.winterWorld = v),
			slider("max_snow_layers", 1, 8, config.maxSnowLayers, "", v -> config.maxSnowLayers = v),
			toggle("melt_on_clear_days", config.meltOnClearDays, v -> config.meltOnClearDays = v),
			toggle("freeze_water", config.freezeWater, v -> config.freezeWater = v),
			toggle("icicles", config.icicles, v -> config.icicles = v),
			toggle("snow_on_panes", config.snowOnPanes, v -> config.snowOnPanes = v),
			toggle("calm_blizzards", config.calmBlizzards, v -> config.calmBlizzards = v)
		);

		this.list.addHeader(Component.translatable(PREFIX + "section.visuals"));
		this.list.addSmall(
			toggle("wintry_colors", config.wintryColors, v -> config.wintryColors = v),
			slider("wintry_color_strength", 0, 100, config.wintryColorStrength, "%", v -> config.wintryColorStrength = v),
			toggle("snowflakes", config.snowflakes, v -> config.snowflakes = v),
			slider("snowflake_density", 0, 300, config.snowflakeDensity, "%", v -> config.snowflakeDensity = v),
			toggle("winter_fog", config.winterFog, v -> config.winterFog = v),
			slider("fog_strength", 0, 100, config.fogStrength, "%", v -> config.fogStrength = v),
			toggle("warm_glow", config.warmGlow, v -> config.warmGlow = v),
			slider("warm_glow_strength", 0, 100, config.warmGlowStrength, "%", v -> config.warmGlowStrength = v),
			toggle("glow_halos", config.glowHalos, v -> config.glowHalos = v),
			slider("halo_strength", 0, 100, config.haloStrength, "%", v -> config.haloStrength = v),
			toggle("snowy_leaf_tops", config.snowyLeafTops, v -> config.snowyLeafTops = v),
			toggle("winter_sky", config.winterSky, v -> config.winterSky = v),
			toggle("frosty_breath", config.frostyBreath, v -> config.frostyBreath = v)
		);

		this.list.addHeader(Component.translatable(PREFIX + "section.weather"));
		this.list.addSmall(
			toggle("wind_push", config.windPush, v -> config.windPush = v),
			slider("wind_push_strength", 0, 30, config.windPushStrength, "%", v -> config.windPushStrength = v)
		);

		this.list.addHeader(Component.translatable(PREFIX + "section.sounds"));
		this.list.addSmall(
			toggle("wind_sounds", config.windSounds, v -> config.windSounds = v),
			slider("wind_volume", 0, 200, config.windVolume, "%", v -> config.windVolume = v),
			toggle("snow_crunch", config.snowCrunch, v -> config.snowCrunch = v),
			toggle("fire_crackle", config.fireCrackle, v -> config.fireCrackle = v),
			toggle("muffle_indoors", config.muffleIndoors, v -> config.muffleIndoors = v)
		);
	}

	private OptionInstance<Boolean> toggle(String key, boolean value, Consumer<Boolean> setter) {
		OptionInstance<Boolean> option = OptionInstance.createBoolean(PREFIX + key, tooltip(key), value);
		appliers.add(() -> setter.accept(option.get()));
		return option;
	}

	private OptionInstance<Integer> slider(String key, int min, int max, int value, String unit, Consumer<Integer> setter) {
		OptionInstance<Integer> option = new OptionInstance<>(
			PREFIX + key,
			tooltip(key),
			(caption, v) -> Options.genericValueLabel(caption, Component.literal(v + unit)),
			new OptionInstance.IntRange(min, max),
			value,
			v -> {
			}
		);
		appliers.add(() -> setter.accept(option.get()));
		return option;
	}

	private static <T> OptionInstance.TooltipSupplier<T> tooltip(String key) {
		return OptionInstance.cachedConstantTooltip(Component.translatable(PREFIX + key + ".tooltip"));
	}

	@Override
	public void onClose() {
		if (this.list != null) {
			this.list.applyUnsavedChanges();
		}
		appliers.forEach(Runnable::run);
		WinterConfig.save();
		// Biome colors are baked into chunk meshes, so rebuild them when the colors change.
		if (colorsBefore != config.wintryColors || colorStrengthBefore != config.wintryColorStrength || winterBefore != config.winterWorld
			|| leafTopsBefore != config.snowyLeafTops) {
			if (this.minecraft.level != null) {
				this.minecraft.levelExtractor.allChanged();
			}
		}
		super.onClose();
	}
}
