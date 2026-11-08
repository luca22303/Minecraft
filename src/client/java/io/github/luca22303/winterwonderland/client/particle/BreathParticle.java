package io.github.luca22303.winterwonderland.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

/** A soft puff of frosty breath that expands, rises a little and fades. */
public class BreathParticle extends SingleQuadParticle {
	private final SpriteSet sprites;
	private final float startSize;

	protected BreathParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd, SpriteSet sprites) {
		super(level, x, y, z, sprites.first());
		this.sprites = sprites;
		this.xd = xd;
		this.yd = yd;
		this.zd = zd;
		this.gravity = -0.015F;
		this.friction = 0.9F;
		this.hasPhysics = false;
		this.lifetime = 18 + this.random.nextInt(12);
		this.startSize = 0.06F + this.random.nextFloat() * 0.04F;
		this.quadSize = this.startSize;
		this.setColor(0.93F, 0.95F, 0.98F);
		this.alpha = 0.35F;
		this.updateSprite();
	}

	private void updateSprite() {
		// Vanilla's generic sprites go from big to small; run them backwards so the puff expands.
		this.setSprite(this.sprites.get(this.lifetime - this.age, this.lifetime));
	}

	@Override
	public SingleQuadParticle.Layer getLayer() {
		return SingleQuadParticle.Layer.TRANSLUCENT;
	}

	@Override
	public void tick() {
		super.tick();
		if (this.removed) {
			return;
		}
		float progress = (float) this.age / this.lifetime;
		this.quadSize = this.startSize * (1.0F + progress * 2.5F);
		this.alpha = 0.35F * (1.0F - progress);
		this.updateSprite();
	}

	public static class Provider implements ParticleProvider<SimpleParticleType> {
		private final SpriteSet sprites;

		public Provider(SpriteSet sprites) {
			this.sprites = sprites;
		}

		@Override
		public Particle createParticle(SimpleParticleType options, ClientLevel level, double x, double y, double z,
									   double xd, double yd, double zd, RandomSource random) {
			return new BreathParticle(level, x, y, z, xd, yd, zd, this.sprites);
		}
	}
}
