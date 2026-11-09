package io.github.luca22303.winterwonderland.client.mixin;

import io.github.luca22303.winterwonderland.config.WinterConfig;
import io.github.luca22303.winterwonderland.winter.WinterClimate;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.attribute.EnvironmentAttributeSystem;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Crisper, deeper blue skies on clear winter days. */
@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin {
	@Inject(method = "addEnvironmentAttributeLayers", at = @At("RETURN"))
	private void winterwonderland$winterSky(EnvironmentAttributeSystem.Builder builder, CallbackInfoReturnable<EnvironmentAttributeSystem.Builder> cir) {
		builder.addPositionalLayer(EnvironmentAttributes.SKY_COLOR, this::winterwonderland$crispSky);
	}

	@Unique
	private Vector3fc winterwonderland$crispSky(Vector3fc sky, Vec3 pos, Object biomeInterpolator) {
		if (!WinterConfig.get().winterSky) {
			return sky;
		}
		ClientLevel level = (ClientLevel) (Object) this;
		if (WinterClimate.active(level.getBiome(BlockPos.containing(pos)).value()) == WinterClimate.UNTOUCHED) {
			return sky;
		}
		// Only on clear days - snowfall already greys the sky out.
		float amount = 0.6F * (1.0F - level.getRainLevel(1.0F));
		if (amount <= 0.0F) {
			return sky;
		}
		Vector3f crisp = new Vector3f(sky.x() * 0.82F, sky.y() * 0.94F, Math.min(1.0F, sky.z() * 1.06F));
		return new Vector3f(sky).lerp(crisp, amount);
	}
}
