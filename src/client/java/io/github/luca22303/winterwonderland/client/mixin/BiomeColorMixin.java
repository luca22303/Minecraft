package io.github.luca22303.winterwonderland.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.luca22303.winterwonderland.client.WinterColors;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Wintry grass and foliage colors (biome-tinted blocks). */
@Mixin(Biome.class)
public abstract class BiomeColorMixin {
	@ModifyReturnValue(method = "getGrassColor", at = @At("RETURN"))
	private int winterwonderland$frostGrass(int color) {
		return WinterColors.grass((Biome) (Object) this, color);
	}

	@ModifyReturnValue(method = "getFoliageColor", at = @At("RETURN"))
	private int winterwonderland$frostFoliage(int color) {
		return WinterColors.foliage((Biome) (Object) this, color);
	}

	@ModifyReturnValue(method = "getDryFoliageColor", at = @At("RETURN"))
	private int winterwonderland$frostDryFoliage(int color) {
		return WinterColors.dryFoliage((Biome) (Object) this, color);
	}
}
