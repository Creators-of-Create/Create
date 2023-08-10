package com.simibubi.create.content.logistics.chute;

import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;

import net.createmod.catnip.utility.VecHelper;
import net.createmod.catnip.utility.math.AngleHelper;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class SmartChuteFilterSlotPositioning extends ValueBoxTransform.Sided {

	@Override
	public Vec3 getLocalOffset(BlockState state) {
		Direction side = getSide();
		float horizontalAngle = AngleHelper.horizontalAngle(side);
		Vec3 southLocation = VecHelper.voxelSpace(8, 11, 15.5f);
		return VecHelper.rotateCentered(southLocation, horizontalAngle, Axis.Y);
	}

	@Override
	protected boolean isSideActive(BlockState state, Direction direction) {
		return direction.getAxis()
			.isHorizontal();
	}

	@Override
	protected Vec3 getSouthLocation() {
		return Vec3.ZERO;
	}

}
