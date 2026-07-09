package com.simibubi.create.content.decoration.steamWhistle;

import java.util.Map;
import java.util.WeakHashMap;

import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.decoration.steamWhistle.WhistleBlock.WhistleSize;
import com.simibubi.create.content.kinetics.steamEngine.SteamJetParticleData;

import net.createmod.catnip.api.math.AngleHelper;
import net.createmod.catnip.api.math.VecHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class WhistleClient {
	private static final Map<WhistleBlockEntity, WhistleSoundInstance> SOUND_INSTANCES = new WeakHashMap<>();

	public static void tickAudio(WhistleBlockEntity blockEntity, WhistleSize size, boolean powered) {
		WhistleSoundInstance soundInstance = SOUND_INSTANCES.get(blockEntity);
		if (!powered) {
			if (soundInstance != null) {
				soundInstance.fadeOut();
				SOUND_INSTANCES.remove(blockEntity);
			}
			return;
		}

		Level level = blockEntity.getLevel();
		BlockPos worldPosition = blockEntity.getBlockPos();
		float f = (float) Math.pow(2, -blockEntity.pitch / 12.0);
		boolean particle = level.getGameTime() % 8 == 0;
		Vec3 eyePosition = Minecraft.getInstance().getCameraEntity().getEyePosition();
		float maxVolume = (float) Mth.clamp((64 - eyePosition.distanceTo(Vec3.atCenterOf(worldPosition))) / 64, 0, 1);

		if (soundInstance == null || soundInstance.isStopped() || soundInstance.getOctave() != size) {
			soundInstance = new WhistleSoundInstance(size, worldPosition);
			Minecraft.getInstance()
				.getSoundManager()
				.play(soundInstance);
			SOUND_INSTANCES.put(blockEntity, soundInstance);
			AllSoundEvents.WHISTLE_CHIFF.playAt(level, worldPosition, maxVolume * .175f,
				size == WhistleSize.SMALL ? f + .75f : f, false);
			particle = true;
		}

		soundInstance.keepAlive();
		soundInstance.setPitch(f);

		if (!particle)
			return;

		Direction facing = blockEntity.getBlockState()
			.getOptionalValue(WhistleBlock.FACING)
			.orElse(Direction.SOUTH);
		float angle = 180 + AngleHelper.horizontalAngle(facing);
		Vec3 sizeOffset = VecHelper.rotate(new Vec3(0, -0.4f, 1 / 16f * size.ordinal()), angle, Axis.Y);
		Vec3 offset = VecHelper.rotate(new Vec3(0, 1, 0.75f), angle, Axis.Y);
		Vec3 v = offset.scale(.45f)
			.add(sizeOffset)
			.add(Vec3.atCenterOf(worldPosition));
		Vec3 m = offset.subtract(Vec3.atLowerCornerOf(facing.getUnitVec3i())
			.scale(.75f));
		level.addParticle(new SteamJetParticleData(1), v.x, v.y, v.z, m.x, m.y, m.z);
	}

}
