package com.simibubi.create.content.kinetics.deployer;

import com.jozufozu.flywheel.util.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;

import net.createmod.catnip.utility.VecHelper;
import net.createmod.catnip.utility.math.AngleHelper;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class DeployerFilterSlot extends ValueBoxTransform.Sided {

	@Override
	public Vec3 getLocalOffset(BlockState state) {
		Direction facing = state.getValue(DeployerBlock.FACING);
		Vec3 vec = VecHelper.voxelSpace(8f, 8f, 15.5f);

		vec = VecHelper.rotateCentered(vec, AngleHelper.horizontalAngle(getSide()), Axis.Y);
		vec = VecHelper.rotateCentered(vec, AngleHelper.verticalAngle(getSide()), Axis.X);
		vec = vec.subtract(Vec3.atLowerCornerOf(facing.getNormal())
			.scale(2 / 16f));

		return vec;
	}

	@Override
	protected boolean isSideActive(BlockState state, Direction direction) {
		Direction facing = state.getValue(DeployerBlock.FACING);
		if (direction.getAxis() == facing.getAxis())
			return false;
		if (((DeployerBlock) state.getBlock()).getRotationAxis(state) == direction.getAxis())
			return false;
		return true;
	}

	@Override
	public void rotate(BlockState state, PoseStack ms) {
		Direction facing = getSide();
		float xRot = facing == Direction.UP ? 90 : facing == Direction.DOWN ? 270 : 0;
		float yRot = AngleHelper.horizontalAngle(facing) + 180;

		if (facing.getAxis() == Axis.Y)
			TransformStack.cast(ms)
				.rotateY(180 + AngleHelper.horizontalAngle(state.getValue(DeployerBlock.FACING)));

		TransformStack.cast(ms)
			.rotateY(yRot)
			.rotateX(xRot);
	}

	@Override
	protected Vec3 getSouthLocation() {
		return Vec3.ZERO;
	}

}
