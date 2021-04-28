package com.simibubi.create.foundation.sound;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.foundation.sound.SoundScapes.AmbienceGroup;
import com.simibubi.create.foundation.sound.SoundScapes.PitchGroup;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

class SoundScape {
	List<ContinuousSound> continuous;
	List<RepeatingSound> repeating;
	private float pitch;
	private AmbienceGroup group;
	private Vector3d meanPos;
	private PitchGroup pitchGroup;

	public SoundScape(float pitch, AmbienceGroup group) {
		this.pitchGroup = SoundScapes.getGroupFromPitch(pitch);
		this.pitch = pitch;
		this.group = group;
		continuous = new ArrayList<>();
		repeating = new ArrayList<>();
	}

	public SoundScape continuous(SoundEvent sound, float relativeVolume, float relativePitch) {
		return add(new ContinuousSound(sound, this, pitch * relativePitch, relativeVolume));
	}

	public SoundScape repeating(SoundEvent sound, float relativeVolume, float relativePitch, int delay) {
		return add(new RepeatingSound(sound, this, pitch * relativePitch, relativeVolume, delay));
	}

	public SoundScape add(ContinuousSound continuousSound) {
		continuous.add(continuousSound);
		return this;
	}

	public SoundScape add(RepeatingSound repeatingSound) {
		repeating.add(repeatingSound);
		return this;
	}

	public void play() {
		continuous.forEach(Minecraft.getInstance()
			.getSoundHandler()::play);
	}

	public void tick() {
		if (AnimationTickHolder.getTicks() % SoundScapes.UPDATE_INTERVAL == 0)
			meanPos = null;
		repeating.forEach(RepeatingSound::tick);
	}

	public void remove() {
		continuous.forEach(ContinuousSound::remove);
	}

	public Vector3d getMeanPos() {
		return meanPos == null ? meanPos = determineMeanPos() : meanPos;
	}

	private Vector3d determineMeanPos() {
		meanPos = Vector3d.ZERO;
		int amount = 0;
		for (BlockPos blockPos : SoundScapes.getAllLocations(group, pitchGroup)) {
			meanPos = meanPos.add(VecHelper.getCenterOf(blockPos));
			amount++;
		}
		if (amount == 0)
			return meanPos;
		return meanPos.scale(1f / amount);
	}

	public float getVolume() {
		int soundCount = SoundScapes.getSoundCount(group, pitchGroup);
		float argMax = (float) SoundScapes.SOUND_VOLUME_ARG_MAX;
		return MathHelper.clamp(soundCount / (argMax * 10f), 0.075f, .15f);
	}

}