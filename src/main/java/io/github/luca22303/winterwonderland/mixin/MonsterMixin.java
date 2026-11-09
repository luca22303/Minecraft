package io.github.luca22303.winterwonderland.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.github.luca22303.winterwonderland.config.WinterConfig;
import net.minecraft.world.entity.monster.Monster;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Blizzards are vanilla thunderstorms under the hood, and thunderstorms let monsters spawn in daylight.
 * For a cozy winter, blizzards keep the normal daylight rules.
 */
@Mixin(Monster.class)
public abstract class MonsterMixin {
	@ModifyExpressionValue(
		method = "isDarkEnoughToSpawn",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;isThundering()Z")
	)
	private static boolean winterwonderland$calmBlizzards(boolean thundering) {
		WinterConfig config = WinterConfig.get();
		return thundering && !(config.winterWorld && config.calmBlizzards);
	}
}
