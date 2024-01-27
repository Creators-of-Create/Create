package com.simibubi.create.content.kinetics.steamEngine;

import com.jozufozu.flywheel.lib.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.Pointing;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class SteamEngineValueBox extends ValueBoxTransform.Sided {

	@Override
	protected boolean isSideActive(BlockState state, Direction side) {
		Direction engineFacing = SteamEngineBlock.getFacing(state);
		if (engineFacing.getAxis() == side.getAxis())
			return false;

		float roll = 0;
		for (Pointing p : Pointing.values())
			if (p.getCombinedDirection(engineFacing) == side)
				roll = p.getXRotation();
		if (engineFacing == Direction.UP)
			roll += 180;

		boolean recessed = roll % 180 == 0;
		if (engineFacing.getAxis() == Axis.Y)
			recessed ^= state.getValue(SteamEngineBlock.FACING)
				.getAxis() == Axis.X;

		return !recessed;
	}

	@Override
	public Vec3 getLocalOffset(BlockState state) {
		Direction side = getSide();
		Direction engineFacing = SteamEngineBlock.getFacing(state);

		float roll = 0;
		for (Pointing p : Pointing.values())
			if (p.getCombinedDirection(engineFacing) == side)
				roll = p.getXRotation();
		if (engineFacing == Direction.UP)
			roll += 180;

		float horizontalAngle = AngleHelper.horizontalAngle(engineFacing);
		float verticalAngle = AngleHelper.verticalAngle(engineFacing);
		Vec3 local = VecHelper.voxelSpace(8, 14.5, 9);

		local = VecHelper.rotateCentered(local, roll, Axis.Z);
		local = VecHelper.rotateCentered(local, horizontalAngle, Axis.Y);
		local = VecHelper.rotateCentered(local, verticalAngle, Axis.X);

		return local;
	}

	@Override
	public void rotate(BlockState state, PoseStack ms) {
		Direction facing = SteamEngineBlock.getFacing(state);

		if (facing.getAxis() == Axis.Y) {
			super.rotate(state, ms);
			return;
		}

		float roll = 0;
		for (Pointing p : Pointing.values())
			if (p.getCombinedDirection(facing) == getSide())
				roll = p.getXRotation();

		float yRot = AngleHelper.horizontalAngle(facing) + (facing == Direction.DOWN ? 180 : 0);
		TransformStack.of(ms)
			.rotateYDegrees(yRot)
			.rotateXDegrees(facing == Direction.DOWN ? -90 : 90)
			.rotateYDegrees(roll);
	}

	@Override
	protected Vec3 getSouthLocation() {
		return Vec3.ZERO;
	}

}
