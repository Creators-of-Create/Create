package com.simibubi.create.modules.logistics.block;

import com.simibubi.create.foundation.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.Vec3d;

public class RedstoneLinkFrequencySlot extends ValueBoxTransform.Dual {

	public RedstoneLinkFrequencySlot(boolean first) {
		super(first);
	}

	Vec3d horizontal = VecHelper.voxelSpace(10f, 5.5f, 2.5f);
	Vec3d vertical = VecHelper.voxelSpace(10f, 2.5f, 5.5f);

	@Override
	protected Vec3d getLocation(BlockState state) {
		Direction facing = state.get(RedstoneLinkBlock.FACING);
		Vec3d location = vertical;

		if (facing.getAxis().isHorizontal()) {
			location = horizontal;
			if (!isFirst())
				location = location.add(0, 5 / 16f, 0);
			return rotateHorizontally(state, location);
		}

		if (!isFirst())
			location = location.add(0, 0, 5 / 16f);
		location = VecHelper.rotateCentered(location, facing == Direction.DOWN ? 180 : 0, Axis.X);
		return location;
	}

	@Override
	protected Vec3d getOrientation(BlockState state) {
		Direction facing = state.get(RedstoneLinkBlock.FACING);
		float yRot = facing.getAxis().isVertical() ? 180 : AngleHelper.horizontalAngle(facing);
		float zRot = facing == Direction.UP ? 90 : facing == Direction.DOWN ? 270 : 0;
		return new Vec3d(0, yRot + 180, zRot);
	}
	
	@Override
	protected float getScale() {
		return .5f;
	}

}
