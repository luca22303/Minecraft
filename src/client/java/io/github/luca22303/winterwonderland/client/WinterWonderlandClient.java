package io.github.luca22303.winterwonderland.client;

import io.github.luca22303.winterwonderland.ModParticles;
import io.github.luca22303.winterwonderland.client.ambience.WinterAmbience;
import io.github.luca22303.winterwonderland.client.particle.BreathParticle;
import io.github.luca22303.winterwonderland.client.particle.DriftingSnowflakeParticle;
import io.github.luca22303.winterwonderland.winter.WinterClimate;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ColorResolverRegistry;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class WinterWonderlandClient implements ClientModInitializer {
	/** Vanilla's fixed spruce and birch leaf colors (these leaves ignore biome colors). */
	private static final int SPRUCE_LEAVES = -10380959;
	private static final int BIRCH_LEAVES = -8345771;

	@Override
	public void onInitializeClient() {
		// The client gets its own copy of the biome registry from the server: classify it too.
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> WinterClimate.classifyAll(handler.registryAccess()));

		ClientTickEvents.END_CLIENT_TICK.register(WinterAmbience::tick);

		ParticleProviderRegistry particles = ParticleProviderRegistry.getInstance();
		particles.register(ModParticles.SNOWFLAKE, DriftingSnowflakeParticle.Provider::new);
		particles.register(ModParticles.BREATH, BreathParticle.Provider::new);

		registerFrostedLeaves(Blocks.SPRUCE_LEAVES, SPRUCE_LEAVES);
		registerFrostedLeaves(Blocks.BIRCH_LEAVES, BIRCH_LEAVES);
	}

	/** Spruce and birch leaves have a fixed color; give them the same winter frost as other leaves. */
	private static void registerFrostedLeaves(Block leaves, int baseColor) {
		ColorResolver resolver = (biome, x, z) -> WinterColors.foliage(biome, baseColor);
		ColorResolverRegistry.register(resolver);
		BlockColorRegistry.register(List.of(new BlockTintSource() {
			@Override
			public int color(BlockState state) {
				return baseColor;
			}

			@Override
			public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
				return level.getBlockTint(pos, resolver);
			}
		}), leaves);
	}
}
