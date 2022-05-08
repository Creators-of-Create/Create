package com.simibubi.create.content.contraptions.components.steam.whistle;

import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.contraptions.components.steam.whistle.WhistleBlock.WhistleSize;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class WhistleSoundInstance extends AbstractTickableSoundInstance {

	private boolean active;
	private int keepAlive;
	private WhistleSize size;

	public WhistleSoundInstance(WhistleSize size, BlockPos worldPosition) {
		super(
			(size == WhistleSize.SMALL ? AllSoundEvents.WHISTLE_HIGH
				: size == WhistleSize.MEDIUM ? AllSoundEvents.WHISTLE : AllSoundEvents.WHISTLE_LOW).getMainEvent(),
			SoundSource.RECORDS);
		this.size = size;
		looping = true;
		active = true;
		volume = 0.05f;
		delay = 0;
		keepAlive();
		Vec3 v = Vec3.atCenterOf(worldPosition);
		x = v.x;
		y = v.y;
		z = v.z;
	}

	public WhistleSize getOctave() {
		return size;
	}

	public void fadeOut() {
		this.active = false;
	}

	public void keepAlive() {
		keepAlive = 2;
	}

	public void setPitch(float pitch) {
		this.pitch = pitch;
	}

	@Override
	public void tick() {
		Vec3 eyePosition = Minecraft.getInstance().cameraEntity.getEyePosition();
		float maxVolume = (float) Mth.clamp((30 - eyePosition.distanceTo(new Vec3(x, y, z))) / 30, 0, .75f);
		if (active) {
			volume = Math.min(1, volume + .25f);
			volume = Math.min(volume, maxVolume);
			keepAlive--;
			if (keepAlive == 0)
				fadeOut();
			return;

		}
		volume = Math.max(0, volume - .25f);
		volume = Math.min(volume, maxVolume);
		if (volume == 0)
			stop();
	}

}
