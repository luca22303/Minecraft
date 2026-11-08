package io.github.luca22303.winterwonderland.block;

import io.github.luca22303.winterwonderland.WinterWonderland;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

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

	private ModBlocks() {
	}

	private static Block register(String name, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties properties) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, WinterWonderland.id(name));
		return Registry.register(BuiltInRegistries.BLOCK, key, factory.apply(properties.setId(key)));
	}

	public static void init() {
		// Loads the class, which registers the blocks.
	}
}
