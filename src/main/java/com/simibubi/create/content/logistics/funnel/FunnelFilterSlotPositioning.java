package com.simibubi.create.content.logistics.funnel;

import dev.engine_room.flywheel.lib.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.logistics.funnel.BeltFunnelBlock.Shape;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class FunnelFilterSlotPositioning extends ValueBoxTransform.Sided {

	@Override
	public Vec3 getLocalOffset(BlockState state) {
		Direction side = getSide();
		float horizontalAngle = AngleHelper.horizontalAngle(side);
		Direction funnelFacing = FunnelBlock.getFunnelFacing(state);
		float stateAngle = AngleHelper.horizontalAngle(funnelFacing);

		if (state.getBlock() instanceof BeltFunnelBlock) {
			switch (state.getValue(BeltFunnelBlock.SHAPE)) {

			case EXTENDED:
				return VecHelper.rotateCentered(VecHelper.voxelSpace(8, 15.5f, 13), stateAngle, Axis.Y);
			case PULLING:
			case PUSHING:
				return VecHelper.rotateCentered(VecHelper.voxelSpace(8, 12.0f, 8.675f), horizontalAngle, Axis.Y);
			default:
			case RETRACTED:
				return VecHelper.rotateCentered(VecHelper.voxelSpace(8, 13, 7.5f), horizontalAngle, Axis.Y);
			}
		}

		if (!funnelFacing.getAxis()
			.isHorizontal()) {
			Vec3 southLocation = VecHelper.voxelSpace(8, funnelFacing == Direction.DOWN ? 14 : 2, 15.5f);
			return VecHelper.rotateCentered(southLocation, horizontalAngle, Axis.Y);
		}

		return VecHelper.rotateCentered(VecHelper.voxelSpace(8, 12.2, 8.55f), horizontalAngle, Axis.Y);
	}

	@Override
	public void rotate(BlockState state, PoseStack ms) {
		Direction facing = FunnelBlock.getFunnelFacing(state);

		if (facing.getAxis()
			.isVertical()) {
			super.rotate(state, ms);
			return;
		}

		boolean isBeltFunnel = state.getBlock() instanceof BeltFunnelBlock;
		if (isBeltFunnel && state.getValue(BeltFunnelBlock.SHAPE) != Shape.EXTENDED) {
			Shape shape = state.getValue(BeltFunnelBlock.SHAPE);
			super.rotate(state, ms);
			if (shape == Shape.PULLING || shape == Shape.PUSHING)
				TransformStack.of(ms)
					.rotateXDegrees(-22.5f);
			return;
		}

		if (state.getBlock() instanceof FunnelBlock) {
			super.rotate(state, ms);
			TransformStack.of(ms)
				.rotateXDegrees(-22.5f);
			return;
		}

		float yRot = AngleHelper.horizontalAngle(AbstractFunnelBlock.getFunnelFacing(state))
			+ (facing == Direction.DOWN ? 180 : 0);
		TransformStack.of(ms)
			.rotateYDegrees(yRot)
			.rotateXDegrees(facing == Direction.DOWN ? -90 : 90);
	}

	@Override
	protected boolean isSideActive(BlockState state, Direction direction) {
		Direction facing = FunnelBlock.getFunnelFacing(state);

		if (facing == null)
			return false;
		if (facing.getAxis()
			.isVertical())
			return direction.getAxis()
				.isHorizontal();
		if (state.getBlock() instanceof BeltFunnelBlock && state.getValue(BeltFunnelBlock.SHAPE) == Shape.EXTENDED)
			return direction == Direction.UP;
		return direction == facing;
	}

	@Override
	protected Vec3 getSouthLocation() {
		return Vec3.ZERO;
	}

}
