package io.github.luca22303.winterwonderland.client;

import io.github.luca22303.winterwonderland.config.WinterConfig;
import io.github.luca22303.winterwonderland.winter.WinterClimate;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.biome.Biome;

/** Muted, frosty winter colors for grass and foliage. */
public final class WinterColors {
	/** Pale, slightly blue-ish sage that grass is pulled towards. */
	private static final int FROSTED_GRASS = 0xA9B7A6;
	/** Pale frosted green for leaves - reads like hoarfrost on the canopy. */
	private static final int FROSTED_FOLIAGE = 0x9DB2A6;
	/** Dry foliage (leaf litter) gets a dusty, faded brown. */
	private static final int FROSTED_DRY_FOLIAGE = 0xA89C8C;

	private WinterColors() {
	}

	public static int grass(Biome biome, int color) {
		return frost(biome, color, FROSTED_GRASS);
	}

	public static int foliage(Biome biome, int color) {
		return frost(biome, color, FROSTED_FOLIAGE);
	}

	public static int dryFoliage(Biome biome, int color) {
		return frost(biome, color, FROSTED_DRY_FOLIAGE);
	}

	private static int frost(Biome biome, int color, int frostColor) {
		float amount = amount(biome);
		if (amount <= 0.0F) {
			return color;
		}
		return ARGB.color(ARGB.alpha(color) == 0 ? 255 : ARGB.alpha(color),
			lerp(ARGB.red(color), ARGB.red(frostColor), amount),
			lerp(ARGB.green(color), ARGB.green(frostColor), amount),
			lerp(ARGB.blue(color), ARGB.blue(frostColor), amount));
	}

	/** How much frost a biome gets, 0..1. */
	public static float amount(Biome biome) {
		WinterConfig config = WinterConfig.get();
		if (!config.wintryColors) {
			return 0.0F;
		}
		float strength = config.wintryColorStrength / 100.0F;
		return switch (WinterClimate.active(biome)) {
			case COLD -> strength;
			case TEMPERATE -> strength * 0.75F;
			case WARM -> strength * 0.15F;
			case UNTOUCHED -> 0.0F;
		};
	}

	private static int lerp(int from, int to, float amount) {
		return Math.round(from + (to - from) * amount);
	}
}
