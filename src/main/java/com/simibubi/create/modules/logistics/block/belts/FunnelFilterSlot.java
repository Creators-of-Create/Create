package com.simibubi.create.modules.logistics.block.belts;

import com.simibubi.create.foundation.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.modules.logistics.block.extractor.ExtractorBlock;

import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.Vec3d;

public class FunnelFilterSlot extends ValueBoxTransform {
	
	Vec3d offsetForHorizontal = VecHelper.voxelSpace(8f, 14f, 13.5f);
	Vec3d offsetForBelt = VecHelper.voxelSpace(8f, 8.5f, 14f);
	Vec3d offsetForUpward = VecHelper.voxelSpace(8f, 13.5f, 2f);
	Vec3d offsetForDownward = VecHelper.voxelSpace(8f, 2.5f, 2f);

	@Override
	protected Vec3d getLocation(BlockState state) {
		Vec3d vec = offsetForHorizontal;
		float yRot = AngleHelper.horizontalAngle(state.get(ExtractorBlock.HORIZONTAL_FACING));
		if (AttachedLogisticalBlock.isVertical(state))
			vec = state.get(AttachedLogisticalBlock.UPWARD) ? offsetForUpward : offsetForDownward;
		else if (state.get(FunnelBlock.BELT))
			vec = offsetForBelt;

		return VecHelper.rotateCentered(vec, yRot, Axis.Y);
	}

	@Override
	protected Vec3d getOrientation(BlockState state) {
		Direction blockFacing = AttachedLogisticalBlock.getBlockFacing(state);
		boolean vertical = AttachedLogisticalBlock.isVertical(state);
		float horizontalAngle = AngleHelper.horizontalAngle(state.get(ExtractorBlock.HORIZONTAL_FACING));

		float yRot = blockFacing == Direction.DOWN ? horizontalAngle + 180 : horizontalAngle;
		float zRot = (vertical || state.get(FunnelBlock.BELT)) ? 90 : 0;

		if (blockFacing == Direction.UP)
			zRot += 180;

		return new Vec3d(0, yRot, zRot);
	}

}
