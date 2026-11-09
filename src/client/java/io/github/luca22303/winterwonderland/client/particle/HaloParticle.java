package io.github.luca22303.winterwonderland.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.jspecify.annotations.Nullable;

/**
 * A soft, glowing halo that sits around a light source. It lives as long as the halo system keeps it
 * alive, smoothly follows its target brightness and fades out when released.
 */
public class HaloParticle extends SingleQuadParticle {
	/** Sprites of the halo texture, captured from the particle provider. */
	private static @Nullable SpriteSet sprites;

	private final float flicker;
	private float targetAlpha;
	private boolean released;

	public HaloParticle(ClientLevel level, double x, double y, double z, float size, float r, float g, float b, boolean flickers) {
		super(level, x, y, z, sprites.first());
		this.gravity = 0.0F;
		this.friction = 1.0F;
		this.hasPhysics = false;
		this.xd = 0.0;
		this.yd = 0.0;
		this.zd = 0.0;
		this.lifetime = Integer.MAX_VALUE;
		this.quadSize = size;
		this.alpha = 0.0F;
		this.flicker = flickers ? 1.0F : 0.0F;
		this.setColor(r, g, b);
	}

	public static boolean isReady() {
		return sprites != null;
	}

	public void setTargetAlpha(float alpha) {
		this.targetAlpha = alpha;
	}

	/** Lets the halo fade out and disappear. */
	public void release() {
		this.released = true;
	}

	@Override
	public SingleQuadParticle.Layer getLayer() {
		return SingleQuadParticle.Layer.TRANSLUCENT;
	}

	@Override
	public void tick() {
		this.xo = this.x;
		this.yo = this.y;
		this.zo = this.z;
		float target = this.released ? 0.0F : this.targetAlpha;
		if (this.flicker > 0.0F && !this.released) {
			target *= 0.9F + 0.1F * Mth.sin(this.age * 0.35F + this.random.nextFloat() * 0.6F);
		}
		this.alpha += (target - this.alpha) * 0.08F;
		this.age++;
		if (this.released && this.alpha < 0.004F) {
			this.remove();
		}
	}

	@Override
	protected int getLightCoords(float partialTick) {
		return LightCoordsUtil.FULL_BRIGHT;
	}

	/** Registered only to receive the sprites; halos are created by the halo system, not by commands. */
	public static class Provider implements ParticleProvider<SimpleParticleType> {
		public Provider(SpriteSet spriteSet) {
			sprites = spriteSet;
		}

		@Override
		public @Nullable Particle createParticle(SimpleParticleType options, ClientLevel level, double x, double y, double z,
												 double xd, double yd, double zd, RandomSource random) {
			return null;
		}
	}
}
