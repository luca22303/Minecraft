package io.github.luca22303.winterwonderland.block;

import io.github.luca22303.winterwonderland.config.WinterConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A snow cap on a fence post, plus thin lines of snow on the top rails. The rails end 1 pixel below the
 * top of the block, so their snow is drawn reaching down into the fence block.
 */
public class FenceSnowBlock extends LedgeSnowBlock {
	private static final VoxelShape POST = Block.box(5.5, 0.0, 5.5, 10.5, 2.0, 10.5);

	public FenceSnowBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.withSides(false, false, false, false));
	}

	@Override
	public boolean supports(BlockState ledge) {
		return ledge.getBlock() instanceof FenceBlock;
	}

	@Override
	public BlockState shapedFor(BlockState fence) {
		return this.withSides(fence.getValue(FenceBlock.NORTH), fence.getValue(FenceBlock.EAST),
			fence.getValue(FenceBlock.SOUTH), fence.getValue(FenceBlock.WEST));
	}

	@Override
	protected boolean enabled(WinterConfig config) {
		return config.snowOnFencesAndWalls;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return POST;
	}
}
