package io.github.luca22303.winterwonderland.block;

import io.github.luca22303.winterwonderland.WinterWonderland;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public final class ModBlocks {
	/** Grows naturally only - there is deliberately no item for it. */
	public static final Block ICICLE = register("icicle", IcicleBlock::new, BlockBehaviour.Properties.of()
		.mapColor(MapColor.ICE)
		.noCollision()
		.noOcclusion()
		.instabreak()
		.randomTicks()
		.replaceable()
		.noLootTable()
		.sound(SoundType.GLASS)
		.pushReaction(PushReaction.POPPED));

	/** Snow lying on top of glass panes and iron bars. Forms naturally only. */
	public static final LedgeSnowBlock PANE_SNOW = (LedgeSnowBlock) register("pane_snow", PaneSnowBlock::new, ledgeSnowProperties());
	/** Snow on fence posts and rails. Forms naturally only. */
	public static final LedgeSnowBlock FENCE_SNOW = (LedgeSnowBlock) register("fence_snow", FenceSnowBlock::new, ledgeSnowProperties());
	/** Snow on top of walls. Forms naturally only. */
	public static final LedgeSnowBlock WALL_SNOW = (LedgeSnowBlock) register("wall_snow", WallSnowBlock::new, ledgeSnowProperties());

	public static final List<LedgeSnowBlock> LEDGE_SNOW = List.of(PANE_SNOW, FENCE_SNOW, WALL_SNOW);

	private ModBlocks() {
	}

	private static Block register(String name, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties properties) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, WinterWonderland.id(name));
		return Registry.register(BuiltInRegistries.BLOCK, key, factory.apply(properties.setId(key)));
	}

	private static BlockBehaviour.Properties ledgeSnowProperties() {
		return BlockBehaviour.Properties.of()
			.mapColor(MapColor.SNOW)
			.noCollision()
			.noOcclusion()
			.instabreak()
			.randomTicks()
			.replaceable()
			.noLootTable()
			.sound(SoundType.SNOW)
			.pushReaction(PushReaction.POPPED);
	}

	/** The snow block that can lie on the given ledge (pane, fence, wall), if any. */
	public static Optional<LedgeSnowBlock> ledgeSnowFor(BlockState ledge) {
		for (LedgeSnowBlock snow : LEDGE_SNOW) {
			if (snow.supports(ledge)) {
				return Optional.of(snow);
			}
		}
		return Optional.empty();
	}

	public static void init() {
		// Loads the class, which registers the blocks.
	}
}
