package io.github.luca22303.winterwonderland.block;

import io.github.luca22303.winterwonderland.config.WinterConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.WallSide;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Snow on top of a wall: a cap on the wall post (if it has one) and snow along the wall segments. The
 * segments are 2 pixels lower than the post, so their snow reaches down into the wall block.
 */
public class WallSnowBlock extends LedgeSnowBlock {
	public static final BooleanProperty POST = BlockStateProperties.UP;
	private static final VoxelShape POST_SHAPE = Block.box(3.5, 0.0, 3.5, 12.5, 2.0, 12.5);

	public WallSnowBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.withSides(false, false, false, false).setValue(POST, true));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(POST);
	}

	@Override
	public boolean supports(BlockState ledge) {
		return ledge.getBlock() instanceof WallBlock;
	}

	@Override
	public BlockState shapedFor(BlockState wall) {
		return this.withSides(
				wall.getValue(WallBlock.NORTH) != WallSide.NONE,
				wall.getValue(WallBlock.EAST) != WallSide.NONE,
				wall.getValue(WallBlock.SOUTH) != WallSide.NONE,
				wall.getValue(WallBlock.WEST) != WallSide.NONE)
			.setValue(POST, wall.getValue(WallBlock.UP));
	}

	@Override
	protected boolean enabled(WinterConfig config) {
		return config.snowOnFencesAndWalls;
	}

	/** Only the post cap sticks up into this block; the segment snow lies inside the wall block below. */
	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return state.getValue(POST) ? POST_SHAPE : Shapes.empty();
	}
}
