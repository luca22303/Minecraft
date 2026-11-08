package io.github.luca22303.winterwonderland.winter;

/**
 * Duck interface mixed into {@link net.minecraft.world.level.biome.Biome} so every biome
 * remembers how the winter affects it.
 */
public interface WinterBiome {
	WinterClimate winterwonderland$getClimate();

	void winterwonderland$setClimate(WinterClimate climate);

	boolean winterwonderland$isOcean();

	void winterwonderland$setOcean(boolean ocean);
}
