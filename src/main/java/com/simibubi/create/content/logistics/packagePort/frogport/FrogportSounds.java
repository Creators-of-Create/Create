package com.simibubi.create.content.logistics.packagePort.frogport;

import com.simibubi.create.AllSoundEvents;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class FrogportSounds {

	public void open(Level level, BlockPos pos) {
		AllSoundEvents.FROGPORT_OPEN.playAt(level, Vec3.atCenterOf(pos), 0.5f, 1, false);
	}

	public void close(Level level, BlockPos pos) {
		if (!isPlayerNear(pos))
			return;
		AllSoundEvents.FROGPORT_CLOSE.playAt(level, Vec3.atCenterOf(pos), 1.0f, 1.25f + level.getRandom().nextFloat() * 0.25f,
			true);
	}

	public void catchPackage(Level level, BlockPos pos) {
		if (!isPlayerNear(pos))
			return;
		AllSoundEvents.FROGPORT_CATCH.playAt(level, Vec3.atCenterOf(pos), 1, 1, false);
	}

	public void depositPackage(Level level, BlockPos pos) {
		if (!isPlayerNear(pos))
			return;
		AllSoundEvents.FROGPORT_DEPOSIT.playAt(level, Vec3.atCenterOf(pos), 1, 1, false);
	}

	private boolean isPlayerNear(BlockPos pos) {
		return pos.closerThan(Minecraft.getInstance().player.blockPosition(), 20);
	}

}
