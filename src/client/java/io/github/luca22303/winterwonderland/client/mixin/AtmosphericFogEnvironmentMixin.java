package io.github.luca22303.winterwonderland.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.luca22303.winterwonderland.client.ambience.WinterAmbience;
import io.github.luca22303.winterwonderland.config.WinterConfig;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.AtmosphericFogEnvironment;
import net.minecraft.util.Mth;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Soft misty fog while it snows, thick white-out fog in blizzards, with a cold blue-grey tint. */
@Mixin(AtmosphericFogEnvironment.class)
public abstract class AtmosphericFogEnvironmentMixin {
	@Inject(method = "setupFog", at = @At("TAIL"))
	private void winterwonderland$winterFog(FogData fog, Camera camera, ClientLevel level, float renderDistance, DeltaTracker deltaTracker, CallbackInfo ci) {
		float snow = winterwonderland$snowAmount();
		float blizzard = winterwonderland$blizzardAmount();
		if (snow <= 0.0F && blizzard <= 0.0F) {
			return;
		}
		// Gentle snowfall: haze closes in to ~10 chunks. Blizzard: a white-out at ~2 chunks.
		float end = Mth.lerp(snow, fog.environmentalEnd, Math.min(fog.environmentalEnd, 160.0F));
		end = Mth.lerp(blizzard, end, Math.min(end, 30.0F));
		float start = Mth.lerp(Math.max(snow * 0.5F, blizzard), fog.environmentalStart, Math.min(fog.environmentalStart, 0.0F));
		fog.environmentalEnd = end;
		fog.environmentalStart = Math.min(start, end * 0.5F);
		fog.skyEnd = Mth.lerp(blizzard, fog.skyEnd, Math.min(fog.skyEnd, end * 1.5F));
		fog.cloudEnd = Mth.lerp(blizzard, fog.cloudEnd, Math.min(fog.cloudEnd, end * 2.0F));
	}

	@ModifyReturnValue(method = "getBaseColor", at = @At("RETURN"))
	private Vector3fc winterwonderland$coldFogColor(Vector3fc color) {
		float amount = Math.min(1.0F, winterwonderland$snowAmount() * 0.35F + winterwonderland$blizzardAmount() * 0.65F);
		if (amount <= 0.0F) {
			return color;
		}
		float luminance = color.x() * 0.3F + color.y() * 0.59F + color.z() * 0.11F;
		// Blizzards look brighter and whiter, snowfall a little blue-grey.
		float lift = winterwonderland$blizzardAmount() * 0.08F * Math.min(1.0F, luminance * 3.0F);
		Vector3f cold = new Vector3f(luminance * 0.94F + lift, luminance * 0.98F + lift, luminance * 1.06F + lift);
		return new Vector3f(color).lerp(cold, amount);
	}

	private static float winterwonderland$strength() {
		WinterConfig config = WinterConfig.get();
		return config.winterFog ? config.fogStrength / 100.0F * WinterAmbience.outdoorFactor() : 0.0F;
	}

	private static float winterwonderland$snowAmount() {
		return WinterAmbience.snowFactor() * winterwonderland$strength();
	}

	private static float winterwonderland$blizzardAmount() {
		return WinterAmbience.blizzardFactor() * winterwonderland$strength();
	}
}
