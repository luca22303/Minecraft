package io.github.luca22303.winterwonderland.winter;

import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Global wind used by blizzards. The direction drifts slowly over a few in-game days and is computed
 * from the game time, so client and server always agree without any networking.
 */
public final class Wind {
	private Wind() {
	}

	/** Horizontal unit vector the wind is blowing towards. */
	public static Vec3 direction(long gameTime) {
		double t = gameTime / 6000.0;
		double angle = Math.sin(t * 0.7) * 2.0 + Math.sin(t * 0.23 + 1.3) * 1.5 + 0.8;
		return new Vec3(Math.cos(angle), 0.0, Math.sin(angle));
	}

	public static Vec3 direction(Level level) {
		return direction(level.getGameTime());
	}

	/** 0 when calm, 1 in a full blizzard (vanilla "thunder" while it snows). */
	public static float blizzardIntensity(Level level, float partialTick) {
		return Mth.clamp(level.getThunderLevel(partialTick) * level.getRainLevel(partialTick), 0.0F, 1.0F);
	}

	/** 0 when clear, 1 in full snowfall. */
	public static float snowIntensity(Level level, float partialTick) {
		return Mth.clamp(level.getRainLevel(partialTick), 0.0F, 1.0F);
	}
}
