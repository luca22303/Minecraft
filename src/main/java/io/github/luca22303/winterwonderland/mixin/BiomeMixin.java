package io.github.luca22303.winterwonderland.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.luca22303.winterwonderland.config.WinterConfig;
import io.github.luca22303.winterwonderland.winter.WinterBiome;
import io.github.luca22303.winterwonderland.winter.WinterClimate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * The heart of the winter: lowers biome temperatures (graded by climate), lets light snow fall in warm
 * biomes without sticking, and keeps oceans open except along the shore.
 */
@Mixin(Biome.class)
public abstract class BiomeMixin implements WinterBiome {
	@Unique
	private WinterClimate winterwonderland$climate;
	@Unique
	private boolean winterwonderland$ocean;

	@Shadow
	public abstract float getBaseTemperature();

	@Override
	public WinterClimate winterwonderland$getClimate() {
		if (winterwonderland$climate == null) {
			winterwonderland$climate = WinterClimate.fromTemperature(getBaseTemperature());
		}
		return winterwonderland$climate;
	}

	@Override
	public void winterwonderland$setClimate(WinterClimate climate) {
		winterwonderland$climate = climate;
	}

	@Override
	public boolean winterwonderland$isOcean() {
		return winterwonderland$ocean;
	}

	@Override
	public void winterwonderland$setOcean(boolean ocean) {
		winterwonderland$ocean = ocean;
	}

	@Unique
	private WinterClimate winterwonderland$active() {
		return WinterConfig.get().winterWorld ? winterwonderland$getClimate() : WinterClimate.UNTOUCHED;
	}

	@ModifyReturnValue(method = "getHeightAdjustedTemperature", at = @At("RETURN"))
	private float winterwonderland$coolDown(float temperature) {
		return winterwonderland$active().adjustTemperature(temperature, getBaseTemperature());
	}

	/** Warm biomes (even deserts that never rain) get snowfall. */
	@Inject(method = "getPrecipitationAt", at = @At("HEAD"), cancellable = true)
	private void winterwonderland$snowInWarmBiomes(BlockPos pos, int seaLevel, CallbackInfoReturnable<Biome.Precipitation> cir) {
		if (winterwonderland$active() == WinterClimate.WARM) {
			cir.setReturnValue(Biome.Precipitation.SNOW);
		}
	}

	/** Snow in warm biomes melts on landing. */
	@Inject(method = "shouldSnow", at = @At("HEAD"), cancellable = true)
	private void winterwonderland$noSnowCoverInWarmBiomes(LevelReader level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
		if (winterwonderland$active() == WinterClimate.WARM) {
			cir.setReturnValue(false);
		}
	}

	@ModifyReturnValue(method = "shouldFreeze(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;Z)Z", at = @At("RETURN"))
	private boolean winterwonderland$freezeRules(boolean freezes, LevelReader level, BlockPos pos, boolean checkNeighbors) {
		// Vanilla-cold biomes (frozen oceans, snowy plains...) keep their vanilla behaviour.
		if (!freezes || !winterwonderland$active().isSnowy() || getBaseTemperature() < WinterClimate.SNOW_TEMPERATURE) {
			return freezes;
		}
		if (!WinterConfig.get().freezeWater) {
			return false;
		}
		// Oceans only freeze right next to land, so the open sea stays open.
		return !winterwonderland$ocean || winterwonderland$touchesLand(level, pos);
	}

	@Unique
	private static boolean winterwonderland$touchesLand(LevelReader level, BlockPos pos) {
		for (Direction direction : Direction.Plane.HORIZONTAL) {
			BlockPos neighbour = pos.relative(direction);
			if (!level.hasChunkAt(neighbour)) {
				continue;
			}
			BlockState state = level.getBlockState(neighbour);
			if (state.getFluidState().isEmpty() && !state.isAir() && !state.is(net.minecraft.tags.BlockTags.ICE)) {
				return true;
			}
		}
		return false;
	}
}
