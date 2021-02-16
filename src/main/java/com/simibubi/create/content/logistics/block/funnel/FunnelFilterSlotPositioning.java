package com.simibubi.create.content.logistics.block.funnel;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.content.logistics.block.funnel.BeltFunnelBlock.Shape;
import com.simibubi.create.foundation.tileEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.MatrixStacker;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.BlockState;
import net.minecraft.state.properties.AttachFace;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.vector.Vector3d;

public class FunnelFilterSlotPositioning extends ValueBoxTransform.Sided {

	@Override
	protected Vector3d getLocalOffset(BlockState state) {
		Direction side = getSide();
		float horizontalAngle = AngleHelper.horizontalAngle(side);
		Direction funnelFacing = FunnelBlock.getFunnelFacing(state);
		float stateAngle = AngleHelper.horizontalAngle(funnelFacing);

		if (state.getBlock() instanceof BeltFunnelBlock) {
			switch (state.get(BeltFunnelBlock.SHAPE)) {

			case EXTENDED:
				return VecHelper.rotateCentered(VecHelper.voxelSpace(8, 15.5f, 13), stateAngle, Axis.Y);
			case PULLING:
			case PUSHING:
				return VecHelper.rotateCentered(VecHelper.voxelSpace(8, 12.1, 8.7f), horizontalAngle, Axis.Y);
			default:
			case RETRACTED:
				return VecHelper.rotateCentered(VecHelper.voxelSpace(8, 13, 7.5f), horizontalAngle, Axis.Y);
			}
		}

		if (state.getBlock() instanceof FunnelBlock) {
			if (state.get(FunnelBlock.FACE) == AttachFace.WALL)
				return VecHelper.rotateCentered(VecHelper.voxelSpace(8, 12.1, 8.7f), horizontalAngle, Axis.Y);
			Vector3d southLocation = VecHelper.voxelSpace(8, funnelFacing == Direction.DOWN ? 6.5f : 9.5f, 13f);
			return VecHelper.rotateCentered(southLocation,
				AngleHelper.horizontalAngle(state.get(AbstractFunnelBlock.HORIZONTAL_FACING)), Axis.Y);
		}

		return Vector3d.ZERO;

//		if (!funnelFacing.getAxis()
//			.isHorizontal()) {
//			Vector3d southLocation = VecHelper.voxelSpace(8, funnelFacing == Direction.DOWN ? 3 : 13, 15.5f);
//			return VecHelper.rotateCentered(southLocation, horizontalAngle, Axis.Y);
//		}
//
//		Direction verticalDirection = DirectionHelper.rotateAround(getSide(), funnelFacing.rotateY()
//			.getAxis());
//		if (funnelFacing.getAxis() == Axis.Z)
//			verticalDirection = verticalDirection.getOpposite();
//		float yRot = -AngleHelper.horizontalAngle(verticalDirection) + 180;
//		float xRot = -90;
//		boolean alongX = funnelFacing.getAxis() == Axis.X;
//		float zRotLast = alongX ^ funnelFacing.getAxisDirection() == AxisDirection.POSITIVE ? 180 : 0;
//
//		Vector3d vec = VecHelper.voxelSpace(8, 13, .5f);
//		vec = vec.subtract(.5, .5, .5);
//		vec = VecHelper.rotate(vec, zRotLast, Axis.Z);
//		vec = VecHelper.rotate(vec, yRot, Axis.Y);
//		vec = VecHelper.rotate(vec, alongX ? 0 : xRot, Axis.X);
//		vec = VecHelper.rotate(vec, alongX ? xRot : 0, Axis.Z);
//		vec = vec.add(.5, .5, .5);
//		return vec;
	}

	@Override
	protected void rotate(BlockState state, MatrixStack ms) {
		Direction facing = FunnelBlock.getFunnelFacing(state);

//		if (facing.getAxis()
//			.isVertical()) {
//			super.rotate(state, ms);
//			return;
//		}

		boolean isBeltFunnel = state.getBlock() instanceof BeltFunnelBlock;
		if (isBeltFunnel && state.get(BeltFunnelBlock.SHAPE) != Shape.EXTENDED) {
			Shape shape = state.get(BeltFunnelBlock.SHAPE);
			super.rotate(state, ms);
			if (shape == Shape.PULLING || shape == Shape.PUSHING)
				MatrixStacker.of(ms)
					.rotateX(-22.5f);
			return;
		}

		if (state.getBlock() instanceof FunnelBlock) {
			if (state.get(FunnelBlock.FACE) == AttachFace.WALL) {
				super.rotate(state, ms);
				MatrixStacker.of(ms)
					.rotateX(-22.5f);
				return;
			}
		}

		float yRot = AngleHelper.horizontalAngle(state.get(AbstractFunnelBlock.HORIZONTAL_FACING))
			+ (facing == Direction.DOWN ? 180 : 0);
		MatrixStacker.of(ms)
			.rotateY(yRot)
			.rotateX(facing == Direction.DOWN ? -90 : 90);
	}

	@Override
	protected boolean isSideActive(BlockState state, Direction direction) {
		Direction facing = FunnelBlock.getFunnelFacing(state);

		if (facing == null)
			return false;
		if (state.getBlock() instanceof BeltFunnelBlock && state.get(BeltFunnelBlock.SHAPE) == Shape.EXTENDED)
			return direction == Direction.UP;
		return direction == facing;
	}

	@Override
	protected Vector3d getSouthLocation() {
		return Vector3d.ZERO;
	}

}
