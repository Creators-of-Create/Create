package com.simibubi.create.foundation.sound;

import net.minecraft.client.audio.TickableSound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;

public class ContinuousSound extends TickableSound {

	private float sharedPitch;
	private SoundScape scape;
	private float relativeVolume;

	protected ContinuousSound(SoundEvent event, SoundScape scape, float sharedPitch, float relativeVolume) {
		super(event, SoundCategory.AMBIENT);
		this.scape = scape;
		this.sharedPitch = sharedPitch;
		this.relativeVolume = relativeVolume;
		this.repeat = true;
		this.repeatDelay = 0;
		this.global = false;
	}

	public void remove() {
		setDone();
	}

	@Override
	public float getVolume() {
		return scape.getVolume() * relativeVolume;
	}

	@Override
	public float getPitch() {
		return sharedPitch;
	}

	@Override
	public double getX() {
		return scape.getMeanPos().x;
	}

	@Override
	public double getY() {
		return scape.getMeanPos().y;
	}

	@Override
	public double getZ() {
		return scape.getMeanPos().z;
	}

	@Override
	public void tick() {}

}
