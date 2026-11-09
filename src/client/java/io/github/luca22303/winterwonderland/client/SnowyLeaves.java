package io.github.luca22303.winterwonderland.client;

import io.github.luca22303.winterwonderland.config.WinterConfig;
import io.github.luca22303.winterwonderland.winter.WinterClimate;
import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ColorResolverRegistry;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.color.block.BlockTintSources;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * Snow caps on tree canopies and frosted spruce/birch colors.
 *
 * <p>The leaf models (see {@code assets/minecraft/models/block/*_leaves.json}) get a second, slightly
 * larger "snow cap" layer with tint index 1. Its tint is white and opaque on leaves that face the open
 * sky in a snowy biome, and fully transparent elsewhere, so the cap only shows where snow would settle.
 * As a fallback for renderers that ignore tint alpha, the hidden cap is also tinted like the leaves.
 */
public final class SnowyLeaves {
	/** Vanilla's fixed spruce and birch leaf colors (these leaves ignore biome colors). */
	private static final int SPRUCE_LEAVES = -10380959;
	private static final int BIRCH_LEAVES = -8345771;

	/** Blue channel = how snowy the biome is (0..255); biome blending makes it fade smoothly across borders. */
	private static final ColorResolver SNOWINESS = (biome, x, z) -> isSnowy(biome) ? 0xFFFFFF : 0x000000;

	private SnowyLeaves() {
	}

	public static void register() {
		ColorResolverRegistry.register(SNOWINESS);

		BlockTintSource foliage = BlockTintSources.foliage();
		for (Block leaves : List.of(Blocks.OAK_LEAVES, Blocks.JUNGLE_LEAVES, Blocks.ACACIA_LEAVES, Blocks.DARK_OAK_LEAVES, Blocks.MANGROVE_LEAVES)) {
			BlockColorRegistry.register(List.of(foliage, snowCap(foliage)), leaves);
		}
		BlockTintSource spruce = frostedConstant(SPRUCE_LEAVES);
		BlockColorRegistry.register(List.of(spruce, snowCap(spruce)), Blocks.SPRUCE_LEAVES);
		BlockTintSource birch = frostedConstant(BIRCH_LEAVES);
		BlockColorRegistry.register(List.of(birch, snowCap(birch)), Blocks.BIRCH_LEAVES);
	}

	private static boolean isSnowy(Biome biome) {
		return WinterClimate.active(biome).isSnowy() || biome.getBaseTemperature() < WinterClimate.SNOW_TEMPERATURE;
	}

	/** Spruce and birch keep their own color, but get the same winter frost as other leaves. */
	private static BlockTintSource frostedConstant(int baseColor) {
		ColorResolver resolver = (biome, x, z) -> WinterColors.foliage(biome, baseColor);
		ColorResolverRegistry.register(resolver);
		return new BlockTintSource() {
			@Override
			public int color(BlockState state) {
				return baseColor;
			}

			@Override
			public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
				return level.getBlockTint(pos, resolver);
			}
		};
	}

	private static BlockTintSource snowCap(BlockTintSource leafTint) {
		return new BlockTintSource() {
			@Override
			public int color(BlockState state) {
				// Items and other non-world rendering: no snow.
				return ARGB.color(0, leafTint.color(state));
			}

			@Override
			public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
				int leafColor = leafTint.colorInWorld(state, level, pos);
				float snow = WinterConfig.get().snowyLeafTops ? snowAmount(level, pos) : 0.0F;
				if (snow <= 0.0F) {
					return ARGB.color(0, leafColor);
				}
				int rgb = ARGB.srgbLerp(snow, leafColor, 0xFFFFFFFF);
				return ARGB.color(snow >= 0.5F ? 255 : 0, rgb);
			}
		};
	}

	/** 1 for canopy tops under open sky in a snowy biome, fading out across biome borders. */
	private static float snowAmount(BlockAndTintGetter level, BlockPos pos) {
		if (level.getBrightness(LightLayer.SKY, pos.above()) < 15) {
			return 0.0F;
		}
		return (level.getBlockTint(pos, SNOWINESS) & 0xFF) / 255.0F;
	}
}
