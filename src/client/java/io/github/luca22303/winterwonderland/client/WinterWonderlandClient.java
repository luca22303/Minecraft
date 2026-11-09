package io.github.luca22303.winterwonderland.client;

import io.github.luca22303.winterwonderland.ModParticles;
import io.github.luca22303.winterwonderland.client.ambience.WinterAmbience;
import io.github.luca22303.winterwonderland.client.particle.BreathParticle;
import io.github.luca22303.winterwonderland.client.particle.DriftingSnowflakeParticle;
import io.github.luca22303.winterwonderland.client.particle.HaloParticle;
import io.github.luca22303.winterwonderland.winter.WinterClimate;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;

public class WinterWonderlandClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// The client gets its own copy of the biome registry from the server: classify it too.
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> WinterClimate.classifyAll(handler.registryAccess()));

		ClientTickEvents.END_CLIENT_TICK.register(WinterAmbience::tick);

		ParticleProviderRegistry particles = ParticleProviderRegistry.getInstance();
		particles.register(ModParticles.SNOWFLAKE, DriftingSnowflakeParticle.Provider::new);
		particles.register(ModParticles.BREATH, BreathParticle.Provider::new);
		particles.register(ModParticles.HALO, HaloParticle.Provider::new);

		SnowyLeaves.register();
	}
}
