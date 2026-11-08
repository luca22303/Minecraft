package io.github.luca22303.winterwonderland.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.github.luca22303.winterwonderland.config.WinterConfig;
import io.github.luca22303.winterwonderland.winter.SnowAndIce;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {
	/** Replaces the snow accumulation gamerule with our own snow depth setting. */
	@ModifyExpressionValue(
		method = "tickPrecipitation",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/gamerules/GameRules;get(Lnet/minecraft/world/level/gamerules/GameRule;)Ljava/lang/Object;")
	)
	private Object winterwonderland$snowDepth(Object gameRuleValue) {
		WinterConfig config = WinterConfig.get();
		if (config.winterWorld && gameRuleValue instanceof Integer vanillaMax) {
			return Math.max(vanillaMax, config.maxSnowLayers);
		}
		return gameRuleValue;
	}

	/** Runs for one random column per chunk every few seconds: melting and icicles. */
	@Inject(method = "tickPrecipitation", at = @At("TAIL"))
	private void winterwonderland$winterTick(BlockPos pos, CallbackInfo ci) {
		SnowAndIce.tick((ServerLevel) (Object) this, pos);
	}
}
