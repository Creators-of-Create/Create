package com.simibubi.create.content.logistics.block.chute;

import com.simibubi.create.foundation.tileEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.vector.Vector3d;

public class SmartChuteFilterSlotPositioning extends ValueBoxTransform.Sided {

	@Override
	protected Vector3d getLocalOffset(BlockState state) {
		Direction side = getSide();
		float horizontalAngle = AngleHelper.horizontalAngle(side);
		Vector3d southLocation = VecHelper.voxelSpace(8, 12, 15.5f);
		return VecHelper.rotateCentered(southLocation, horizontalAngle, Axis.Y);
	}

	@Override
	protected boolean isSideActive(BlockState state, Direction direction) {
		return direction.getAxis()
			.isHorizontal();
	}

	@Override
	protected Vector3d getSouthLocation() {
		return Vector3d.ZERO;
	}

}
