package io.github.luca22303.winterwonderland.client.ambience;

import io.github.luca22303.winterwonderland.client.particle.HaloParticle;
import io.github.luca22303.winterwonderland.config.WinterConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Soft glowing halos around lanterns, torches, candles, campfires and other non-solid lights near the
 * player. Stronger at night, indoors and in snowfall (light scattering in the air), subtle by day.
 */
public final class GlowHalos {
	private static final int RADIUS = 14;
	private static final int HEIGHT = 7;
	private static final int MAX_HALOS = 48;
	private static final int SCAN_INTERVAL = 30;

	private static final Map<BlockPos, Halo> HALOS = new HashMap<>();
	private static ClientLevel lastLevel;

	private record Halo(HaloParticle particle, int emission) {
	}

	private GlowHalos() {
	}

	public static void tick(Minecraft minecraft, ClientLevel level, Player player) {
		WinterConfig config = WinterConfig.get();
		if (level != lastLevel) {
			HALOS.clear();
			lastLevel = level;
		}
		if (!config.glowHalos || config.haloStrength <= 0 || !HaloParticle.isReady()) {
			releaseAll();
			return;
		}
		if (level.getGameTime() % SCAN_INTERVAL == 0) {
			scan(minecraft, level, player);
		}
		updateBrightness(level, config);
	}

	private static void scan(Minecraft minecraft, ClientLevel level, Player player) {
		BlockPos center = player.blockPosition();
		Set<BlockPos> found = new HashSet<>();
		for (BlockPos pos : BlockPos.betweenClosed(center.offset(-RADIUS, -HEIGHT, -RADIUS), center.offset(RADIUS, HEIGHT, RADIUS))) {
			if (found.size() >= MAX_HALOS) {
				break;
			}
			BlockState state = level.getBlockState(pos);
			if (isHaloLight(state)) {
				BlockPos immutable = pos.immutable();
				found.add(immutable);
				Halo existing = HALOS.get(immutable);
				if (existing == null || !existing.particle().isAlive()) {
					HALOS.put(immutable, createHalo(minecraft, level, immutable, state));
				}
			}
		}
		Iterator<Map.Entry<BlockPos, Halo>> iterator = HALOS.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<BlockPos, Halo> entry = iterator.next();
			if (!found.contains(entry.getKey())) {
				entry.getValue().particle().release();
				iterator.remove();
			}
		}
	}

	/** Lamps you can see into: lanterns, torches, candles, campfires, end rods... but not glowstone cubes or fire. */
	private static boolean isHaloLight(BlockState state) {
		return state.getLightEmission() >= 10
			&& !state.isSolidRender()
			&& state.getFluidState().isEmpty()
			&& !state.is(BlockTags.FIRE);
	}

	private static Halo createHalo(Minecraft minecraft, ClientLevel level, BlockPos pos, BlockState state) {
		String name = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
		float r = 1.0F;
		float g = 0.72F;
		float b = 0.42F;
		if (name.contains("soul")) {
			r = 0.55F;
			g = 0.85F;
			b = 1.0F;
		} else if (name.contains("copper")) {
			r = 0.6F;
			g = 1.0F;
			b = 0.72F;
		}
		double y = pos.getY() + 0.5;
		if (name.contains("torch")) {
			y = pos.getY() + 0.7;
		} else if (name.contains("campfire")) {
			y = pos.getY() + 0.45;
		}
		int emission = state.getLightEmission();
		float size = 0.45F + emission / 15.0F * 0.35F;
		boolean flickers = name.contains("torch") || name.contains("campfire") || name.contains("candle") || name.contains("lantern");
		HaloParticle particle = new HaloParticle(level, pos.getX() + 0.5, y, pos.getZ() + 0.5, size, r, g, b, flickers);
		minecraft.particleEngine.add(particle);
		return new Halo(particle, emission);
	}

	private static void updateBrightness(ClientLevel level, WinterConfig config) {
		float strength = config.haloStrength / 100.0F;
		float night = nightFactor(level);
		float snow = WinterAmbience.snowFactor();
		for (Map.Entry<BlockPos, Halo> entry : HALOS.entrySet()) {
			BlockPos pos = entry.getKey();
			float sheltered = 1.0F - level.getBrightness(LightLayer.SKY, pos) / 15.0F;
			float darkness = Math.max(night, sheltered);
			float alpha = strength * (0.06F + 0.28F * darkness + 0.16F * snow) * (entry.getValue().emission() / 15.0F);
			entry.getValue().particle().setTargetAlpha(Mth.clamp(alpha, 0.0F, 0.6F));
		}
	}

	/** 0 at noon, 1 at night. */
	private static float nightFactor(ClientLevel level) {
		float time = (level.getDefaultClockTime() % 24000L) / 24000.0F;
		float sun = Mth.cos((time - 0.25F) * Mth.TWO_PI);
		return Mth.clamp(0.5F - sun * 1.5F, 0.0F, 1.0F);
	}

	private static void releaseAll() {
		for (Halo halo : HALOS.values()) {
			halo.particle().release();
		}
		HALOS.clear();
	}
}
