package io.github.luca22303.winterwonderland.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.luca22303.winterwonderland.winter.WinterClimate;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.WeatherEffectRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Warm biomes only get light snow: most snow columns are skipped there. */
@Mixin(WeatherEffectRenderer.class)
public abstract class WeatherEffectRendererMixin {
	@WrapOperation(
		method = "extractRenderState",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;getPrecipitationAt(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/biome/Biome$Precipitation;")
	)
	private Biome.Precipitation winterwonderland$lightSnowInWarmBiomes(ClientLevel level, BlockPos pos, Operation<Biome.Precipitation> original) {
		Biome.Precipitation precipitation = original.call(level, pos);
		if (precipitation == Biome.Precipitation.SNOW && WinterClimate.active(level.getBiome(pos).value()) == WinterClimate.WARM) {
			int hash = (pos.getX() * 73856093) ^ (pos.getZ() * 19349663);
			if (Math.floorMod(hash, 10) >= 3) {
				return Biome.Precipitation.NONE;
			}
		}
		return precipitation;
	}
}
