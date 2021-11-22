package com.simibubi.create.content.contraptions.components.deployer;

import com.jozufozu.flywheel.util.transform.MatrixTransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.tileEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class DeployerFilterSlot extends ValueBoxTransform {

	@Override
	protected Vec3 getLocalOffset(BlockState state) {
		Direction facing = state.getValue(DeployerBlock.FACING);
		Vec3 vec = VecHelper.voxelSpace(8f, 13.5f, 11.5f);

		float yRot = AngleHelper.horizontalAngle(facing);
		float xRot = facing == Direction.UP ? 270 : facing == Direction.DOWN ? 90 : 0;
		vec = VecHelper.rotateCentered(vec, yRot, Axis.Y);
		vec = VecHelper.rotateCentered(vec, xRot, Axis.X);

		return vec;
	}

	@Override
	protected void rotate(BlockState state, PoseStack ms) {
		Direction facing = state.getValue(DeployerBlock.FACING);
		float xRot = facing == Direction.UP ? 90 : facing == Direction.DOWN ? 270 : 0;
		float yRot = AngleHelper.horizontalAngle(facing) + 180;
		MatrixTransformStack.of(ms)
			.rotateY(yRot)
			.rotateX(xRot);
	}

}
