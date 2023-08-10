package com.simibubi.create.content.kinetics.fan;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

public class AirCurrentSound extends AbstractTickableSoundInstance {

	private float pitch;

	protected AirCurrentSound(SoundEvent p_i46532_1_, float pitch) {
		super(p_i46532_1_, SoundSource.BLOCKS);
		this.pitch = pitch;
		volume = 0.01f;
		looping = true;
		delay = 0;
		relative = true;
	}

	@Override
	public void tick() {}

	public void setPitch(float pitch) {
		this.pitch = pitch;
	}

	public void fadeIn(float maxVolume) {
		volume = Math.min(maxVolume, volume + .05f);
	}

	public void fadeOut() {
		volume = Math.max(0, volume - .05f);
	}

	public boolean isFaded() {
		return volume == 0;
	}

	@Override
	public float getPitch() {
		return pitch;
	}

	public void stopSound() {
		stop();
	}

}
