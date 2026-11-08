package io.github.luca22303.winterwonderland.client.mixin;

import io.github.luca22303.winterwonderland.config.WinterConfig;
import net.minecraft.client.renderer.LightmapRenderStateExtractor;
import net.minecraft.client.renderer.state.LightmapRenderState;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** A slightly warmer, cozier tint for block light (torches, lanterns, fire). Kept subtle for shaders. */
@Mixin(LightmapRenderStateExtractor.class)
public abstract class LightmapRenderStateExtractorMixin {
	@Inject(method = "extract", at = @At("TAIL"))
	private void winterwonderland$warmGlow(LightmapRenderState renderState, float partialTicks, CallbackInfo ci) {
		WinterConfig config = WinterConfig.get();
		if (!renderState.needsUpdate || !config.warmGlow || config.warmGlowStrength <= 0) {
			return;
		}
		float strength = config.warmGlowStrength / 100.0F;
		renderState.blockLightTint = new Vector3f(renderState.blockLightTint).mul(1.0F, 1.0F - 0.08F * strength, 1.0F - 0.22F * strength);
	}
}
