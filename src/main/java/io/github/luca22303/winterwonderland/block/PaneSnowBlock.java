package io.github.luca22303.winterwonderland.block;

import io.github.luca22303.winterwonderland.config.WinterConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A thin ridge of snow lying on top of a glass pane or iron bars. It follows the pane's shape, forms
 * during snowfall and melts away on clear days or near light and heat.
 */
public class PaneSnowBlock extends Block {
	public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
	public static final BooleanProperty EAST = BlockStateProperties.EAST;
	public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
	public static final BooleanProperty WEST = BlockStateProperties.WEST;

	private static final VoxelShape POST = Block.box(6.5, 0.0, 6.5, 9.5, 2.0, 9.5);
	private static final VoxelShape NORTH_ARM = Block.box(6.5, 0.0, 0.0, 9.5, 2.0, 6.5);
	private static final VoxelShape SOUTH_ARM = Block.box(6.5, 0.0, 9.5, 9.5, 2.0, 16.0);
	private static final VoxelShape WEST_ARM = Block.box(0.0, 0.0, 6.5, 6.5, 2.0, 9.5);
	private static final VoxelShape EAST_ARM = Block.box(9.5, 0.0, 6.5, 16.0, 2.0, 9.5);

	public PaneSnowBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any()
			.setValue(NORTH, false).setValue(EAST, false).setValue(SOUTH, false).setValue(WEST, false));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(NORTH, EAST, SOUTH, WEST);
	}

	public static boolean isPane(BlockState state) {
		return state.getBlock() instanceof IronBarsBlock;
	}

	/** The snow ridge shaped to match the pane below it. */
	public BlockState shapedFor(BlockState pane) {
		return this.defaultBlockState()
			.setValue(NORTH, pane.getValue(IronBarsBlock.NORTH))
			.setValue(EAST, pane.getValue(IronBarsBlock.EAST))
			.setValue(SOUTH, pane.getValue(IronBarsBlock.SOUTH))
			.setValue(WEST, pane.getValue(IronBarsBlock.WEST));
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		VoxelShape shape = POST;
		if (state.getValue(NORTH)) {
			shape = Shapes.or(shape, NORTH_ARM);
		}
		if (state.getValue(SOUTH)) {
			shape = Shapes.or(shape, SOUTH_ARM);
		}
		if (state.getValue(WEST)) {
			shape = Shapes.or(shape, WEST_ARM);
		}
		if (state.getValue(EAST)) {
			shape = Shapes.or(shape, EAST_ARM);
		}
		return shape;
	}

	@Override
	protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		return isPane(level.getBlockState(pos.below()));
	}

	@Override
	protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction direction,
									 BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
		if (direction == Direction.DOWN) {
			return isPane(neighbourState) ? this.shapedFor(neighbourState) : Blocks.AIR.defaultBlockState();
		}
		return super.updateShape(state, level, ticks, pos, direction, neighbourPos, neighbourState, random);
	}

	@Override
	protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		WinterConfig config = WinterConfig.get();
		boolean melting = level.getBrightness(LightLayer.BLOCK, pos) >= 11
			|| !level.getBiome(pos).value().coldEnoughToSnow(pos, level.getSeaLevel())
			|| (!level.isRaining() && level.isBrightOutside() && random.nextInt(10) == 0);
		if (melting || !config.snowOnPanes || !config.winterWorld) {
			level.removeBlock(pos, false);
		}
	}
}
