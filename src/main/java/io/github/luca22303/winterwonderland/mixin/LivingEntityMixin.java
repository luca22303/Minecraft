package io.github.luca22303.winterwonderland.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.luca22303.winterwonderland.config.WinterConfig;
import io.github.luca22303.winterwonderland.winter.WinterClimate;
import io.github.luca22303.winterwonderland.winter.Wind;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Blizzard wind push: walking into the wind is a little slower, with the wind at your back a little
 * faster. Only outdoors, only for players (movement of the local player is simulated on the client).
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
	@ModifyReturnValue(method = "getFrictionInfluencedSpeed", at = @At("RETURN"))
	private float winterwonderland$windPush(float speed) {
		LivingEntity self = (LivingEntity) (Object) this;
		if (!(self instanceof Player) || !self.onGround()) {
			return speed;
		}
		WinterConfig config = WinterConfig.get();
		if (!config.windPush || config.windPushStrength <= 0) {
			return speed;
		}
		Level level = self.level();
		float blizzard = Wind.blizzardIntensity(level, 1.0F);
		if (blizzard <= 0.0F || WinterClimate.active(level.getBiome(self.blockPosition()).value()) == WinterClimate.UNTOUCHED) {
			return speed;
		}
		if (!level.canSeeSky(self.blockPosition().above())) {
			return speed;
		}
		Vec3 motion = self.getDeltaMovement().multiply(1.0, 0.0, 1.0);
		if (motion.lengthSqr() < 1.0E-4) {
			return speed;
		}
		double alignment = motion.normalize().dot(Wind.direction(level));
		float strength = config.windPushStrength / 100.0F * blizzard;
		// Against the wind (alignment < 0): up to -strength. With the wind: up to +strength / 2.
		double factor = alignment < 0.0 ? 1.0 + alignment * strength : 1.0 + alignment * strength * 0.5;
		return (float) (speed * factor);
	}
}
