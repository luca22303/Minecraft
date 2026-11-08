package io.github.luca22303.winterwonderland.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

/** A small snowflake that drifts down slowly, sways, follows the wind and fades out. */
public class DriftingSnowflakeParticle extends SingleQuadParticle {
	private final float swayPhase;
	private final float swaySpeed;

	protected DriftingSnowflakeParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd, SpriteSet sprites) {
		super(level, x, y, z, sprites.get(5 + level.getRandom().nextInt(3), 7));
		this.xd = xd;
		this.yd = yd;
		this.zd = zd;
		this.gravity = 0.0F;
		this.friction = 1.0F;
		this.quadSize = 0.035F + this.random.nextFloat() * 0.035F;
		this.lifetime = 100 + this.random.nextInt(100);
		this.swayPhase = this.random.nextFloat() * Mth.TWO_PI;
		this.swaySpeed = 0.05F + this.random.nextFloat() * 0.08F;
		this.setColor(0.94F, 0.97F, 1.0F);
		this.alpha = 0.0F;
	}

	@Override
	public SingleQuadParticle.Layer getLayer() {
		return SingleQuadParticle.Layer.TRANSLUCENT;
	}

	@Override
	public void tick() {
		super.tick();
		float sway = Mth.sin(this.age * this.swaySpeed + this.swayPhase) * 0.004F;
		this.xd += sway;
		this.zd += Mth.cos(this.age * this.swaySpeed + this.swayPhase) * 0.002F;
		// Fade in quickly, fade out at the end of life.
		float fadeIn = Math.min(1.0F, this.age / 8.0F);
		float fadeOut = Math.min(1.0F, (this.lifetime - this.age) / 20.0F);
		this.alpha = 0.9F * Math.min(fadeIn, fadeOut);
		if (this.onGround && this.age < this.lifetime - 20) {
			this.age = this.lifetime - 20;
			this.xd = 0.0;
			this.zd = 0.0;
		}
	}

	public static class Provider implements ParticleProvider<SimpleParticleType> {
		private final SpriteSet sprites;

		public Provider(SpriteSet sprites) {
			this.sprites = sprites;
		}

		@Override
		public Particle createParticle(SimpleParticleType options, ClientLevel level, double x, double y, double z,
									   double xd, double yd, double zd, RandomSource random) {
			return new DriftingSnowflakeParticle(level, x, y, z, xd, yd, zd, this.sprites);
		}
	}
}
