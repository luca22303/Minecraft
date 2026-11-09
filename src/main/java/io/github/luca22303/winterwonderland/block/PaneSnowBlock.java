package io.github.luca22303.winterwonderland.block;

import io.github.luca22303.winterwonderland.config.WinterConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** A thin ridge of snow on top of a glass pane or iron bars. */
public class PaneSnowBlock extends LedgeSnowBlock {
	private static final VoxelShape POST = Block.box(6.5, 0.0, 6.5, 9.5, 2.0, 9.5);
	private static final VoxelShape NORTH_ARM = Block.box(6.5, 0.0, 0.0, 9.5, 2.0, 6.5);
	private static final VoxelShape SOUTH_ARM = Block.box(6.5, 0.0, 9.5, 9.5, 2.0, 16.0);
	private static final VoxelShape WEST_ARM = Block.box(0.0, 0.0, 6.5, 6.5, 2.0, 9.5);
	private static final VoxelShape EAST_ARM = Block.box(9.5, 0.0, 6.5, 16.0, 2.0, 9.5);

	public PaneSnowBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.withSides(false, false, false, false));
	}

	public static boolean isPane(BlockState state) {
		return state.getBlock() instanceof IronBarsBlock;
	}

	@Override
	public boolean supports(BlockState ledge) {
		return isPane(ledge);
	}

	@Override
	public BlockState shapedFor(BlockState pane) {
		return this.withSides(pane.getValue(IronBarsBlock.NORTH), pane.getValue(IronBarsBlock.EAST),
			pane.getValue(IronBarsBlock.SOUTH), pane.getValue(IronBarsBlock.WEST));
	}

	@Override
	protected boolean enabled(WinterConfig config) {
		return config.snowOnPanes;
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
}
