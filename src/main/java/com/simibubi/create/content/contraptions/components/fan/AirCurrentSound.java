package com.simibubi.create.content.contraptions.components.fan;

import net.minecraft.client.audio.TickableSound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;

public class AirCurrentSound extends TickableSound {

	private float pitch;

	protected AirCurrentSound(SoundEvent p_i46532_1_, float pitch) {
		super(p_i46532_1_, SoundCategory.BLOCKS);
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
