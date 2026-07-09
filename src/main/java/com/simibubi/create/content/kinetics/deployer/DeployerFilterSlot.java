package com.simibubi.create.content.kinetics.deployer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;

import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.api.math.VecHelper;
import net.createmod.catnip.api.math.AngleHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class DeployerFilterSlot extends ValueBoxTransform.Sided {

	@Override
	public Vec3 getLocalOffset(LevelAccessor level, BlockPos pos, BlockState state) {
		Direction facing = state.getValue(DeployerBlock.FACING);
		Vec3 vec = VecHelper.voxelSpace(8f, 8f, 15.5f);

		vec = VecHelper.rotateCentered(vec, AngleHelper.horizontalAngle(getSide()), Axis.Y);
		vec = VecHelper.rotateCentered(vec, AngleHelper.verticalAngle(getSide()), Axis.X);
		vec = vec.subtract(Vec3.atLowerCornerOf(facing.getUnitVec3i())
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
	public void rotate(LevelAccessor level, BlockPos pos, BlockState state, PoseStack ms) {
		Direction facing = getSide();
		float xRot = facing == Direction.UP ? 90 : facing == Direction.DOWN ? 270 : 0;
		float yRot = AngleHelper.horizontalAngle(facing) + 180;

		if (facing.getAxis() == Axis.Y)
			TransformStack.of(ms)
				.rotateYDegrees(180 + AngleHelper.horizontalAngle(state.getValue(DeployerBlock.FACING)));

		TransformStack.of(ms)
			.rotateYDegrees(yRot)
			.rotateXDegrees(xRot);
	}

	@Override
	protected Vec3 getSouthLocation() {
		return Vec3.ZERO;
	}

}
