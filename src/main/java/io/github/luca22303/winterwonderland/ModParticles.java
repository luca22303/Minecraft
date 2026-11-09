package io.github.luca22303.winterwonderland;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;

public final class ModParticles {
	/** A slowly drifting, swaying snowflake. */
	public static final SimpleParticleType SNOWFLAKE = register("snowflake");
	/** A small puff of frosty breath. */
	public static final SimpleParticleType BREATH = register("breath");
	/** A soft glowing halo around a light source (client-side only, managed by the halo system). */
	public static final SimpleParticleType HALO = register("halo");

	private ModParticles() {
	}

	private static SimpleParticleType register(String name) {
		return Registry.register(BuiltInRegistries.PARTICLE_TYPE, WinterWonderland.id(name), FabricParticleTypes.simple());
	}

	public static void init() {
		// Loads the class, which registers the particles.
	}
}
