package io.github.luca22303.winterwonderland.block;

import io.github.luca22303.winterwonderland.config.WinterConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A decorative icicle that grows by itself under roof edges in the cold, in three sizes, and melts
 * away near light/heat or when it gets warmer.
 */
public class IcicleBlock extends Block {
	public static final IntegerProperty SIZE = IntegerProperty.create("size", 1, 3);
	/** Icicles melt at this block light level (torches, lanterns, fire nearby). */
	public static final int MELT_LIGHT_LEVEL = 10;
	private static final VoxelShape[] SHAPES = {
		Block.column(6.0, 10.0, 16.0),
		Block.column(6.0, 5.0, 16.0),
		Block.column(6.0, 0.0, 16.0)
	};

	public IcicleBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(SIZE, 1));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(SIZE);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPES[state.getValue(SIZE) - 1];
	}

	@Override
	protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		BlockPos above = pos.above();
		return level.getBlockState(above).isFaceSturdy(level, above, Direction.DOWN);
	}

	@Override
	protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction direction,
									 BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
		if (direction == Direction.UP && !this.canSurvive(state, level, pos)) {
			return Blocks.AIR.defaultBlockState();
		}
		return super.updateShape(state, level, ticks, pos, direction, neighbourPos, neighbourState, random);
	}

	@Override
	protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		WinterConfig config = WinterConfig.get();
		boolean warm = level.getBrightness(LightLayer.BLOCK, pos) >= MELT_LIGHT_LEVEL
			|| !level.getBiome(pos).value().coldEnoughToSnow(pos, level.getSeaLevel());
		if (!config.icicles || !config.winterWorld || warm) {
			level.removeBlock(pos, false);
			return;
		}
		int size = state.getValue(SIZE);
		if (size < 3 && random.nextInt(6) == 0) {
			level.setBlockAndUpdate(pos, state.setValue(SIZE, size + 1));
		}
	}

	/** Icicles drip a little on sunny days. */
	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		if (random.nextInt(40) == 0 && level.isBrightOutside() && !level.isRaining()) {
			double tipY = pos.getY() + 1.0 - state.getValue(SIZE) * 5.0 / 16.0 - 0.05;
			level.addParticle(ParticleTypes.DRIPPING_WATER, pos.getX() + 0.5, tipY, pos.getZ() + 0.5, 0.0, 0.0, 0.0);
		}
	}
}
