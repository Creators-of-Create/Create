package com.simibubi.create.content.logistics.block.funnel;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.content.logistics.block.AttachedLogisticalBlock;
import com.simibubi.create.content.logistics.block.extractor.ExtractorBlock;
import com.simibubi.create.foundation.tileEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.VecHelper;

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
	protected void rotate(BlockState state, MatrixStack ms) {
		Direction blockFacing = AttachedLogisticalBlock.getBlockFacing(state);
		boolean vertical = AttachedLogisticalBlock.isVertical(state);
		float horizontalAngle = AngleHelper.horizontalAngle(state.get(ExtractorBlock.HORIZONTAL_FACING));

		float yRot = blockFacing == Direction.DOWN ? horizontalAngle + 180 : horizontalAngle;
		float xRot = (vertical || state.get(FunnelBlock.BELT)) ? 90 : 0;

		if (blockFacing == Direction.UP)
			xRot += 180;
		
		ms.multiply(VecHelper.rotateY(yRot));
		ms.multiply(VecHelper.rotateX(xRot));
	}

}
