package com.simibubi.create.content.decoration.steamWhistle;

import static com.simibubi.create.AllSoundEvents.WHISTLE_HIGH;
import static com.simibubi.create.AllSoundEvents.WHISTLE_LOW;
import static com.simibubi.create.AllSoundEvents.WHISTLE_MEDIUM;

import com.simibubi.create.content.decoration.steamWhistle.WhistleBlock.WhistleSize;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

public class WhistleSoundInstance extends AbstractTickableSoundInstance {

	private boolean active;
	private int keepAlive;
	private WhistleSize size;

	public WhistleSoundInstance(WhistleSize size, BlockPos worldPosition) {
		super((size == WhistleSize.SMALL ? WHISTLE_HIGH : size == WhistleSize.MEDIUM ? WHISTLE_MEDIUM : WHISTLE_LOW)
			.getMainEvent(), SoundSource.RECORDS);
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
		if (active) {
			volume = Math.min(1, volume + .25f);
			keepAlive--;
			if (keepAlive == 0)
				fadeOut();
			return;

		}
		volume = Math.max(0, volume - .25f);
		if (volume == 0)
			stop();
	}

}
