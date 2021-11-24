package com.simibubi.create.content.logistics.block.redstone;

import com.jozufozu.flywheel.util.transform.MatrixTransformStack;
import com.jozufozu.flywheel.util.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.tileEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class RedstoneLinkFrequencySlot extends ValueBoxTransform.Dual {

	public RedstoneLinkFrequencySlot(boolean first) {
		super(first);
	}

	Vec3 horizontal = VecHelper.voxelSpace(10f, 5.5f, 2.5f);
	Vec3 vertical = VecHelper.voxelSpace(10f, 2.5f, 5.5f);

	@Override
	protected Vec3 getLocalOffset(BlockState state) {
		Direction facing = state.getValue(RedstoneLinkBlock.FACING);
		Vec3 location = vertical;

		if (facing.getAxis()
			.isHorizontal()) {
			location = horizontal;
			if (isFirst())
				location = location.add(0, 5 / 16f, 0);
			return rotateHorizontally(state, location);
		}

		if (isFirst())
			location = location.add(0, 0, 5 / 16f);
		location = VecHelper.rotateCentered(location, facing == Direction.DOWN ? 180 : 0, Axis.X);
		return location;
	}

	@Override
	protected void rotate(BlockState state, PoseStack ms) {
		Direction facing = state.getValue(RedstoneLinkBlock.FACING);
		float yRot = facing.getAxis()
			.isVertical() ? 0 : AngleHelper.horizontalAngle(facing) + 180;
		float xRot = facing == Direction.UP ? 90 : facing == Direction.DOWN ? 270 : 0;
		TransformStack.cast(ms)
			.rotateY(yRot)
			.rotateX(xRot);
	}

	@Override
	protected float getScale() {
		return .5f;
	}

}
