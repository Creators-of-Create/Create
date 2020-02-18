package com.simibubi.create.modules.contraptions.components.deployer;

import com.simibubi.create.foundation.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.Vec3d;

public class DeployerFilterSlot extends ValueBoxTransform {

	@Override
	protected Vec3d getLocation(BlockState state) {
		Direction facing = state.get(DeployerBlock.FACING);
		Vec3d vec = VecHelper.voxelSpace(8f, 13.5f, 11.5f);

		float yRot = AngleHelper.horizontalAngle(facing);
		float zRot = facing == Direction.UP ? 270 : facing == Direction.DOWN ? 90 : 0;
		vec = VecHelper.rotateCentered(vec, yRot, Axis.Y);
		vec = VecHelper.rotateCentered(vec, zRot, Axis.Z);

		return vec;
	}

	@Override
	protected Vec3d getOrientation(BlockState state) {
		Direction facing = state.get(DeployerBlock.FACING);
		float yRot = AngleHelper.horizontalAngle(facing) + 180;
		float zRot = facing == Direction.UP ? 90 : facing == Direction.DOWN ? 270 : 0;
		return new Vec3d(0, yRot, zRot);
	}

}
