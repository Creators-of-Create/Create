package com.simibubi.create.foundation.sound;

import com.simibubi.create.foundation.utility.AnimationTickHolder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.vector.Vector3d;

public class RepeatingSound {

	private SoundEvent event;
	private float sharedPitch;
	private int repeatDelay;
	private SoundScape scape;
	private float relativeVolume;

	public RepeatingSound(SoundEvent event, SoundScape scape, float sharedPitch, float relativeVolume,
		int repeatDelay) {
		this.event = event;
		this.scape = scape;
		this.sharedPitch = sharedPitch;
		this.relativeVolume = relativeVolume;
		this.repeatDelay = Math.max(1, repeatDelay);
	}

	public void tick() {
		if (AnimationTickHolder.getTicks() % repeatDelay != 0)
			return;

		ClientWorld world = Minecraft.getInstance().level;
		Vector3d meanPos = scape.getMeanPos();

		world.playLocalSound(meanPos.x, meanPos.y, meanPos.z, event, SoundCategory.AMBIENT,
			scape.getVolume() * relativeVolume, sharedPitch, true);
	}

}
