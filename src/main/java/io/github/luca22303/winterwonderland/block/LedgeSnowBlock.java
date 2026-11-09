package io.github.luca22303.winterwonderland.block;

import io.github.luca22303.winterwonderland.config.WinterConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

/**
 * Snow lying on a thin ledge (glass pane, fence, wall). The snow lives in the air block above the ledge,
 * follows its shape, forms during snowfall and melts on clear days or near light and heat.
 *
 * <p>Where a ledge tops out below the full block height (wall segments, fence rails), the model reaches
 * a little down into the block below so the snow still lies on top of it.
 */
public abstract class LedgeSnowBlock extends Block {
	public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
	public static final BooleanProperty EAST = BlockStateProperties.EAST;
	public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
	public static final BooleanProperty WEST = BlockStateProperties.WEST;

	protected LedgeSnowBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(NORTH, EAST, SOUTH, WEST);
	}

	/** Whether this kind of snow can lie on the given block. */
	public abstract boolean supports(BlockState ledge);

	/** The snow shaped to match the ledge below it. */
	public abstract BlockState shapedFor(BlockState ledge);

	/** Whether this kind of snow is switched on in the settings. */
	protected abstract boolean enabled(WinterConfig config);

	protected BlockState withSides(boolean north, boolean east, boolean south, boolean west) {
		return this.defaultBlockState().setValue(NORTH, north).setValue(EAST, east).setValue(SOUTH, south).setValue(WEST, west);
	}

	@Override
	protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		return this.supports(level.getBlockState(pos.below()));
	}

	@Override
	protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction direction,
									 BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
		if (direction == Direction.DOWN) {
			return this.supports(neighbourState) ? this.shapedFor(neighbourState) : Blocks.AIR.defaultBlockState();
		}
		return super.updateShape(state, level, ticks, pos, direction, neighbourPos, neighbourState, random);
	}

	@Override
	protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		WinterConfig config = WinterConfig.get();
		boolean melting = level.getBrightness(LightLayer.BLOCK, pos) >= 11
			|| !level.getBiome(pos).value().coldEnoughToSnow(pos, level.getSeaLevel())
			|| (!level.isRaining() && level.isBrightOutside() && random.nextInt(10) == 0);
		if (melting || !this.enabled(config) || !config.winterWorld) {
			level.removeBlock(pos, false);
		}
	}
}
