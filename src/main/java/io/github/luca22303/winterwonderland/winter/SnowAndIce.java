package io.github.luca22303.winterwonderland.winter;

import io.github.luca22303.winterwonderland.block.IcicleBlock;
import io.github.luca22303.winterwonderland.block.ModBlocks;
import io.github.luca22303.winterwonderland.config.WinterConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

/** Server-side winter world logic that runs alongside vanilla's snow and ice tick. */
public final class SnowAndIce {
	/** Chance (1 in N) per column tick that a layer of deep snow melts on a clear day. */
	private static final int MELT_CHANCE = 6;
	/** Chance (1 in N) per column tick that an icicle tries to form. */
	private static final int ICICLE_CHANCE = 24;

	private SnowAndIce() {
	}

	public static void tick(ServerLevel level, BlockPos randomPos) {
		WinterConfig config = WinterConfig.get();
		if (!config.winterWorld) {
			return;
		}
		BlockPos top = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, randomPos);
		Biome biome = level.getBiome(top).value();
		WinterClimate climate = WinterClimate.of(biome);
		if (!climate.isSnowy()) {
			return;
		}

		if (config.meltOnClearDays && !level.isRaining() && level.isBrightOutside() && level.getRandom().nextInt(MELT_CHANCE) == 0) {
			meltDeepSnow(level, top);
			meltDeepSnow(level, top.below());
		}

		if (config.icicles && level.getRandom().nextInt(ICICLE_CHANCE) == 0) {
			tryFormIcicle(level, top);
		}
	}

	/** Deep snow sinks back by one layer, but never below a single layer. */
	private static void meltDeepSnow(ServerLevel level, BlockPos pos) {
		BlockState state = level.getBlockState(pos);
		if (state.is(Blocks.SNOW)) {
			int layers = state.getValue(SnowLayerBlock.LAYERS);
			if (layers > 1) {
				level.setBlockAndUpdate(pos, state.setValue(SnowLayerBlock.LAYERS, layers - 1));
			}
		}
	}

	/**
	 * Icicles hang from roof eaves and overhangs: the top block of the column has air below it, and that
	 * air spot is next to open sky (so they don't grow on the inside of your ceiling).
	 */
	private static void tryFormIcicle(ServerLevel level, BlockPos top) {
		BlockPos roof = top.below();
		BlockState roofState = level.getBlockState(roof);
		if (roofState.is(Blocks.SNOW)) {
			roof = roof.below();
			roofState = level.getBlockState(roof);
		}
		if (roofState.isAir() || roofState.is(BlockTags.LEAVES) || !roofState.getFluidState().isEmpty()) {
			return;
		}
		BlockPos hangPos = roof.below();
		if (!level.getBlockState(hangPos).isAir() || !roofState.isFaceSturdy(level, roof, Direction.DOWN)) {
			return;
		}
		if (level.getBrightness(LightLayer.BLOCK, hangPos) >= IcicleBlock.MELT_LIGHT_LEVEL) {
			return;
		}
		if (!level.getBiome(hangPos).value().coldEnoughToSnow(hangPos, level.getSeaLevel())) {
			return;
		}
		if (!opensToSky(level, hangPos)) {
			return;
		}
		level.setBlockAndUpdate(hangPos, ModBlocks.ICICLE.defaultBlockState());
	}

	private static boolean opensToSky(ServerLevel level, BlockPos pos) {
		for (Direction direction : Direction.Plane.HORIZONTAL) {
			BlockPos side = pos.relative(direction);
			if (level.getBlockState(side).isAir() && level.canSeeSky(side)) {
				return true;
			}
		}
		return false;
	}
}
