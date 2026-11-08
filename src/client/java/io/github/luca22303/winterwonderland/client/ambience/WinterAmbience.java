package io.github.luca22303.winterwonderland.client.ambience;

import io.github.luca22303.winterwonderland.ModParticles;
import io.github.luca22303.winterwonderland.ModSounds;
import io.github.luca22303.winterwonderland.config.WinterConfig;
import io.github.luca22303.winterwonderland.winter.WinterClimate;
import io.github.luca22303.winterwonderland.winter.Wind;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.golem.AbstractGolem;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Client-side winter ambience, run once per client tick: drifting snowflakes, frosty breath, wind and
 * fire sounds. Also tracks the smoothed weather values the fog uses.
 */
public final class WinterAmbience {
	private static final RandomSource RANDOM = RandomSource.create();
	private static final int BREATH_RANGE = 24;

	private static float outdoor = 1.0F;
	private static float snowFactor;
	private static float blizzardFactor;
	private static WindSoundInstance windSound;

	private WinterAmbience() {
	}

	/** 0 indoors, 1 under open sky (smoothed). */
	public static float outdoorFactor() {
		return outdoor;
	}

	/** Smoothed snowfall strength at the player, 0..1. */
	public static float snowFactor() {
		return snowFactor;
	}

	/** Smoothed blizzard strength at the player, 0..1. */
	public static float blizzardFactor() {
		return blizzardFactor;
	}

	public static void tick(Minecraft minecraft) {
		ClientLevel level = minecraft.level;
		Player player = minecraft.player;
		if (level == null || player == null) {
			snowFactor = 0.0F;
			blizzardFactor = 0.0F;
			windSound = null;
			return;
		}
		if (minecraft.isPaused()) {
			return;
		}

		WinterConfig config = WinterConfig.get();
		BlockPos eyePos = BlockPos.containing(player.getEyePosition());
		WinterClimate climate = WinterClimate.active(level.getBiome(eyePos).value());

		float sky = level.getBrightness(LightLayer.SKY, eyePos) / 15.0F;
		outdoor += (sky - outdoor) * 0.1F;

		float snow = 0.0F;
		float blizzard = 0.0F;
		if (climate != WinterClimate.UNTOUCHED) {
			snow = Wind.snowIntensity(level, 1.0F);
			blizzard = Wind.blizzardIntensity(level, 1.0F);
			if (climate == WinterClimate.WARM) {
				snow *= 0.4F;
				blizzard *= 0.4F;
			}
		}
		snowFactor += (snow - snowFactor) * 0.05F;
		blizzardFactor += (blizzard - blizzardFactor) * 0.05F;

		if (config.snowflakes && climate != WinterClimate.UNTOUCHED) {
			spawnSnowflakes(level, player, climate, config);
		}
		if (config.frostyBreath) {
			spawnBreath(minecraft, level, player);
		}
		updateWindSound(minecraft, level, player, config);
		if (config.fireCrackle && level.getGameTime() % 25 == 0) {
			playFireCrackle(level, player);
		}
	}

	// ---------------------------------------------------------------- snowflakes

	private static void spawnSnowflakes(ClientLevel level, Player player, WinterClimate climate, WinterConfig config) {
		// A few flakes drift through the air even between snowfalls; many more while it snows.
		float calm = climate.isSnowy() ? 0.5F : 0.0F;
		float rate = (calm + snowFactor * 3.0F + blizzardFactor * 14.0F) * config.snowflakeDensity / 100.0F;
		int count = Mth.floor(rate) + (RANDOM.nextFloat() < rate - Mth.floor(rate) ? 1 : 0);
		if (count <= 0) {
			return;
		}
		Vec3 wind = Wind.direction(level);
		double windSpeed = 0.008 + blizzardFactor * 0.35;
		// In a blizzard, spawn flakes upwind so they sweep across your view.
		double upwind = blizzardFactor * 8.0;
		Vec3 center = player.getEyePosition().subtract(wind.scale(upwind));
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		for (int i = 0; i < count; i++) {
			double x = center.x + (RANDOM.nextDouble() - 0.5) * 28.0;
			double y = center.y + RANDOM.nextDouble() * 14.0 - 4.0;
			double z = center.z + (RANDOM.nextDouble() - 0.5) * 28.0;
			pos.set(x, y, z);
			if (!level.isLoaded(pos) || !level.getBlockState(pos).isAir() || !level.canSeeSky(pos)) {
				continue;
			}
			double xd = wind.x * windSpeed + (RANDOM.nextDouble() - 0.5) * 0.01;
			double zd = wind.z * windSpeed + (RANDOM.nextDouble() - 0.5) * 0.01;
			double yd = -0.025 - RANDOM.nextDouble() * 0.03 - blizzardFactor * 0.06;
			level.addParticle(ModParticles.SNOWFLAKE, x, y, z, xd, yd, zd);
		}
	}

	// ---------------------------------------------------------------- breath

	private static void spawnBreath(Minecraft minecraft, ClientLevel level, Player player) {
		long time = level.getGameTime();
		Entity camera = minecraft.getCameraEntity();
		boolean firstPerson = minecraft.options.getCameraType().isFirstPerson();
		for (Entity entity : level.entitiesForRendering()) {
			if (!(entity instanceof LivingEntity living) || !breathes(living)) {
				continue;
			}
			if (entity == camera && firstPerson) {
				continue;
			}
			int period = 50 + Math.floorMod(entity.getId(), 25);
			if (Math.floorMod(time + entity.getId() * 13L, period) != 0) {
				continue;
			}
			if (entity.distanceToSqr(player) > BREATH_RANGE * BREATH_RANGE) {
				continue;
			}
			BlockPos eyePos = BlockPos.containing(living.getEyePosition());
			if (!isCold(level, eyePos)) {
				continue;
			}
			emitBreath(level, living);
		}
	}

	private static boolean breathes(LivingEntity entity) {
		return entity.isAlive()
			&& !entity.isSpectator()
			&& !entity.isInvisible()
			&& !entity.isUnderWater()
			&& !(entity instanceof ArmorStand)
			&& !(entity instanceof AbstractGolem)
			&& !entity.is(EntityTypeTags.UNDEAD);
	}

	/** Cold enough for visible breath: freezing biome temperature and not right next to a fire or lamp. */
	private static boolean isCold(ClientLevel level, BlockPos pos) {
		if (level.getBrightness(LightLayer.BLOCK, pos) >= 11) {
			return false;
		}
		Biome biome = level.getBiome(pos).value();
		return biome.coldEnoughToSnow(pos, level.getSeaLevel());
	}

	private static void emitBreath(ClientLevel level, LivingEntity entity) {
		Vec3 look = Vec3.directionFromRotation(entity.getXRot(), entity.getYHeadRot());
		Vec3 mouth = entity.getEyePosition()
			.add(look.scale(entity.getBbWidth() * 0.5 + 0.12))
			.subtract(0.0, entity.getBbHeight() * 0.06, 0.0);
		for (int i = 0; i < 3; i++) {
			double speed = 0.03 + RANDOM.nextDouble() * 0.02;
			level.addParticle(ModParticles.BREATH,
				mouth.x, mouth.y, mouth.z,
				look.x * speed + (RANDOM.nextDouble() - 0.5) * 0.01,
				look.y * speed + 0.004,
				look.z * speed + (RANDOM.nextDouble() - 0.5) * 0.01);
		}
	}

	// ---------------------------------------------------------------- sounds

	/** Indoors, outdoor weather sounds get much quieter. */
	public static float muffle() {
		return WinterConfig.get().muffleIndoors ? 0.15F + 0.85F * outdoor : 1.0F;
	}

	public static float windVolumeTarget() {
		WinterConfig config = WinterConfig.get();
		if (!config.windSounds) {
			return 0.0F;
		}
		float strength = 0.1F * snowFactor + 0.65F * blizzardFactor;
		return strength * muffle() * config.windVolume / 100.0F;
	}

	private static void updateWindSound(Minecraft minecraft, ClientLevel level, Player player, WinterConfig config) {
		if (windVolumeTarget() > 0.01F && (windSound == null || !minecraft.getSoundManager().isActive(windSound))) {
			windSound = new WindSoundInstance();
			minecraft.getSoundManager().play(windSound);
		}
		// Occasional gusts in a blizzard, coming from upwind.
		if (config.windSounds && blizzardFactor > 0.3F && RANDOM.nextInt(160) == 0) {
			Vec3 from = player.getEyePosition().subtract(Wind.direction(level).scale(6.0)).add(0.0, 2.0, 0.0);
			float volume = 0.5F * blizzardFactor * muffle() * config.windVolume / 100.0F;
			level.playLocalSound(from.x, from.y, from.z, ModSounds.WIND_GUST, SoundSource.WEATHER, volume, 0.8F + RANDOM.nextFloat() * 0.3F, false);
		}
	}

	private static void playFireCrackle(ClientLevel level, Player player) {
		if (RANDOM.nextInt(2) != 0) {
			return;
		}
		BlockPos center = player.blockPosition();
		List<BlockPos> fires = new ArrayList<>();
		for (BlockPos pos : BlockPos.betweenClosed(center.offset(-5, -2, -5), center.offset(5, 4, 5))) {
			if (isLitFire(level.getBlockState(pos))) {
				fires.add(pos.immutable());
			}
		}
		if (fires.isEmpty()) {
			return;
		}
		BlockPos fire = fires.get(RANDOM.nextInt(fires.size()));
		level.playLocalSound(fire, ModSounds.FIRE_CRACKLE, SoundSource.BLOCKS, 0.25F + RANDOM.nextFloat() * 0.2F, 0.9F + RANDOM.nextFloat() * 0.2F, false);
	}

	private static boolean isLitFire(BlockState state) {
		if (state.is(BlockTags.FIRE)) {
			return true;
		}
		if (state.is(BlockTags.CAMPFIRES) || state.getBlock() instanceof AbstractFurnaceBlock) {
			return state.hasProperty(BlockStateProperties.LIT) && state.getValue(BlockStateProperties.LIT);
		}
		return false;
	}
}
