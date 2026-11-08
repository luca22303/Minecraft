package io.github.luca22303.winterwonderland.client.ambience;

import io.github.luca22303.winterwonderland.ModSounds;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

/** The looping wind that follows the listener; volume and pitch follow the weather. */
public class WindSoundInstance extends AbstractTickableSoundInstance {
	public WindSoundInstance() {
		super(ModSounds.WIND_LOOP, SoundSource.WEATHER, RandomSource.create());
		this.looping = true;
		this.delay = 0;
		this.relative = true;
		this.attenuation = SoundInstance.Attenuation.NONE;
		this.volume = 0.0F;
		this.pitch = 0.6F;
	}

	@Override
	public boolean canStartSilent() {
		return true;
	}

	@Override
	public void tick() {
		float target = WinterAmbience.windVolumeTarget();
		this.volume += (target - this.volume) * 0.04F;
		this.pitch = 0.55F + WinterAmbience.blizzardFactor() * 0.3F;
		if (target <= 0.001F && this.volume < 0.003F) {
			this.stop();
		}
	}
}
