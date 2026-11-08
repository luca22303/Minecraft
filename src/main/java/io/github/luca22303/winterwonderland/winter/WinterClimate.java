package io.github.luca22303.winterwonderland.winter;

import io.github.luca22303.winterwonderland.WinterWonderland;
import io.github.luca22303.winterwonderland.config.WinterConfig;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

/**
 * How strongly winter affects a biome. Based on the biome's own temperature, so it works for
 * modded biomes (Terralith etc.) too.
 */
public enum WinterClimate {
	/** Nether, End and anything else that should never get winter. */
	UNTOUCHED,
	/** Already cold biomes (taiga, snowy plains, peaks): heavy snow and blizzards. */
	COLD,
	/** Plains, forests, swamps, oceans...: regular snowfall, snow stays on the ground. */
	TEMPERATE,
	/** Desert, savanna, jungle, badlands: occasional light snow that doesn't stick. */
	WARM;

	/** Vanilla starts snowing below this temperature. */
	public static final float SNOW_TEMPERATURE = 0.15F;

	private static final float COLD_BELOW = 0.35F;
	private static final float WARM_FROM = 0.95F;

	private static final TagKey<Biome> C_IS_NETHER = TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath("c", "is_nether"));
	private static final TagKey<Biome> C_IS_END = TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath("c", "is_end"));
	private static final TagKey<Biome> C_IS_OCEAN = TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath("c", "is_ocean"));

	/** Climate guessed from the biome alone, used until the registry has been classified. */
	public static WinterClimate fromTemperature(float baseTemperature) {
		if (baseTemperature < COLD_BELOW) {
			return COLD;
		}
		return baseTemperature < WARM_FROM ? TEMPERATE : WARM;
	}

	public static WinterClimate of(Biome biome) {
		return ((WinterBiome) (Object) biome).winterwonderland$getClimate();
	}

	public static boolean isOcean(Biome biome) {
		return ((WinterBiome) (Object) biome).winterwonderland$isOcean();
	}

	/** The winter climate in effect right now - {@link #UNTOUCHED} when the winter is switched off. */
	public static WinterClimate active(Biome biome) {
		return WinterConfig.get().winterWorld ? of(biome) : UNTOUCHED;
	}

	public boolean isSnowy() {
		return this == COLD || this == TEMPERATE;
	}

	/**
	 * Lowers a biome's temperature so it snows and freezes. Height variation is kept, so
	 * mountains stay colder than valleys.
	 */
	public float adjustTemperature(float temperature, float baseTemperature) {
		return switch (this) {
			// Make cold biomes properly freezing.
			case COLD -> temperature - 0.6F;
			// Move the biome's sea-level temperature to just below the snow line.
			case TEMPERATE -> temperature - baseTemperature - 0.05F;
			// Warm biomes keep their temperature: snow falls there but doesn't stick.
			case WARM, UNTOUCHED -> temperature;
		};
	}

	/** Assigns a climate to every biome of the given registries, using biome tags to leave the Nether and End alone. */
	public static void classifyAll(RegistryAccess registries) {
		int[] counts = new int[values().length];
		registries.lookupOrThrow(Registries.BIOME).listElements().forEach(holder -> {
			WinterClimate climate = classify(holder);
			WinterBiome biome = (WinterBiome) (Object) holder.value();
			biome.winterwonderland$setClimate(climate);
			biome.winterwonderland$setOcean(holder.is(BiomeTags.IS_OCEAN) || holder.is(C_IS_OCEAN));
			counts[climate.ordinal()]++;
		});
		WinterWonderland.LOGGER.info("Winter climates: {} cold, {} temperate, {} warm, {} untouched biomes",
			counts[COLD.ordinal()], counts[TEMPERATE.ordinal()], counts[WARM.ordinal()], counts[UNTOUCHED.ordinal()]);
	}

	private static WinterClimate classify(Holder<Biome> holder) {
		if (holder.is(BiomeTags.IS_NETHER) || holder.is(BiomeTags.IS_END) || holder.is(C_IS_NETHER) || holder.is(C_IS_END)
			|| holder.is(Biomes.THE_VOID)) {
			return UNTOUCHED;
		}
		return fromTemperature(holder.value().getBaseTemperature());
	}
}
