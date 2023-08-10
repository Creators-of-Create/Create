package com.simibubi.create.foundation.sound;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.foundation.sound.SoundScapes.AmbienceGroup;
import com.simibubi.create.foundation.sound.SoundScapes.PitchGroup;
import com.simibubi.create.infrastructure.config.AllConfigs;

import net.createmod.catnip.utility.AnimationTickHolder;
import net.createmod.catnip.utility.VecHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

class SoundScape {
	List<ContinuousSound> continuous;
	List<RepeatingSound> repeating;
	private float pitch;
	private AmbienceGroup group;
	private Vec3 meanPos;
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
			.getSoundManager()::play);
	}

	public void tick() {
		if (AnimationTickHolder.getTicks() % SoundScapes.UPDATE_INTERVAL == 0)
			meanPos = null;
		repeating.forEach(RepeatingSound::tick);
	}

	public void remove() {
		continuous.forEach(ContinuousSound::remove);
	}

	public Vec3 getMeanPos() {
		return meanPos == null ? meanPos = determineMeanPos() : meanPos;
	}

	private Vec3 determineMeanPos() {
		meanPos = Vec3.ZERO;
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
		Entity renderViewEntity = Minecraft.getInstance().cameraEntity;
		float distanceMultiplier = 0;
		if (renderViewEntity != null) {
			double distanceTo = renderViewEntity.position()
				.distanceTo(getMeanPos());
			distanceMultiplier = (float) Mth.lerp(distanceTo / SoundScapes.MAX_AMBIENT_SOURCE_DISTANCE, 2, 0);
		}
		int soundCount = SoundScapes.getSoundCount(group, pitchGroup);
		float max = AllConfigs.client().ambientVolumeCap.getF();
		float argMax = (float) SoundScapes.SOUND_VOLUME_ARG_MAX;
		return Mth.clamp(soundCount / (argMax * 10f), 0.025f, max) * distanceMultiplier;
	}

}
