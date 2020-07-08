package com.simibubi.create.content.logistics.block.funnel;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.logistics.block.funnel.BeltFunnelBlock.Shape;
import com.simibubi.create.foundation.tileEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.DirectionHelper;
import com.simibubi.create.foundation.utility.MatrixStacker;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.math.Vec3d;

public class FunnelFilterSlotPositioning extends ValueBoxTransform.Sided {

	@Override
	protected Vec3d getLocalOffset(BlockState state) {
		if (AllBlocks.BRASS_BELT_FUNNEL.has(state))
			if (state.get(BeltFunnelBlock.SHAPE) == Shape.RETRACTED)
				return VecHelper.rotateCentered(VecHelper.voxelSpace(8, 13, 7.5f),
					AngleHelper.horizontalAngle(getSide()), Axis.Y);

		Vec3d localOffset =
			getSide() == Direction.UP ? VecHelper.voxelSpace(8, 14.5f, 8) : VecHelper.voxelSpace(8, 1.5f, 8);

		if (getSide().getAxis()
			.isHorizontal()) {
			Vec3d southLocation = VecHelper.voxelSpace(8, 8, 14.5f);
			localOffset = VecHelper.rotateCentered(southLocation, AngleHelper.horizontalAngle(getSide()), Axis.Y);
		}

		if (AllBlocks.BRASS_CHUTE_FUNNEL.has(state)) {
			Direction facing = state.get(ChuteFunnelBlock.HORIZONTAL_FACING);
			localOffset = localOffset.subtract(new Vec3d(facing.getDirectionVec()).scale(2 / 16f));
		}

		return localOffset;
	}

	@Override
	protected void rotate(BlockState state, MatrixStack ms) {
		Direction facing = FunnelBlock.getFunnelFacing(state);

		if (!facing.getAxis()
			.isVertical()
			&& !(AllBlocks.BRASS_BELT_FUNNEL.has(state) && state.get(BeltFunnelBlock.SHAPE) == Shape.RETRACTED)) {
			Direction verticalDirection = DirectionHelper.rotateAround(getSide(), facing.rotateY()
				.getAxis());
			if (facing.getAxis() == Axis.Z)
				verticalDirection = verticalDirection.getOpposite();

			boolean reverse = state.getBlock() instanceof HorizontalInteractionFunnelBlock
				&& !state.get(HorizontalInteractionFunnelBlock.PUSHING);

			float yRot = -AngleHelper.horizontalAngle(verticalDirection) + 180;
			float xRot = -90;
			boolean alongX = facing.getAxis() == Axis.X;
			float zRotLast = alongX ^ facing.getAxisDirection() == AxisDirection.POSITIVE ? 180 : 0;
			if (reverse)
				zRotLast += 180;

			MatrixStacker.of(ms)
				.rotateZ(alongX ? xRot : 0)
				.rotateX(alongX ? 0 : xRot)
				.rotateY(yRot)
				.rotateZ(zRotLast);
			return;
		}

		super.rotate(state, ms);
	}

	@Override
	protected boolean isSideActive(BlockState state, Direction direction) {
		Direction facing = FunnelBlock.getFunnelFacing(state);

		if (facing == null)
			return false;

		if (AllBlocks.BRASS_BELT_FUNNEL.has(state))
			return state.get(BeltFunnelBlock.SHAPE) == Shape.RETRACTED ? direction == facing
				: direction != Direction.DOWN && direction.getAxis() != facing.getAxis();

		return direction.getAxis() != facing.getAxis();
	}

	@Override
	protected Vec3d getSouthLocation() {
		return Vec3d.ZERO;
	}

}
