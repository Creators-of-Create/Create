package com.simibubi.create.content.contraptions.components.deployer;

import com.jozufozu.flywheel.util.transform.MatrixTransformStack;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.foundation.tileEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.vector.Vector3d;

public class DeployerFilterSlot extends ValueBoxTransform {

	@Override
	protected Vector3d getLocalOffset(BlockState state) {
		Direction facing = state.getValue(DeployerBlock.FACING);
		Vector3d vec = VecHelper.voxelSpace(8f, 13.5f, 11.5f);

		float yRot = AngleHelper.horizontalAngle(facing);
		float zRot = facing == Direction.UP ? 270 : facing == Direction.DOWN ? 90 : 0;
		vec = VecHelper.rotateCentered(vec, yRot, Axis.Y);
		vec = VecHelper.rotateCentered(vec, zRot, Axis.Z);

		return vec;
	}

	@Override
	protected void rotate(BlockState state, MatrixStack ms) {
		Direction facing = state.getValue(DeployerBlock.FACING);
		float xRot = facing == Direction.UP ? 90 : facing == Direction.DOWN ? 270 : 0;
		float yRot = AngleHelper.horizontalAngle(facing) + 180;
		MatrixTransformStack.of(ms)
			.rotateY(yRot)
			.rotateX(xRot);
	}

}
