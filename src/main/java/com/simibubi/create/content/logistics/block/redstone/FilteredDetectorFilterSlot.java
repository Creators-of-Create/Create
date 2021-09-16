package com.simibubi.create.content.logistics.block.redstone;

import com.jozufozu.flywheel.util.transform.MatrixTransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.tileEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.phys.Vec3;

public class FilteredDetectorFilterSlot extends ValueBoxTransform {
	Vec3 position = VecHelper.voxelSpace(8f, 15.5f, 11f);

	@Override
	protected Vec3 getLocalOffset(BlockState state) {
		return rotateHorizontally(state, position);
	}

	@Override
	protected void rotate(BlockState state, PoseStack ms) {
		float yRot = AngleHelper.horizontalAngle(state.getValue(HorizontalDirectionalBlock.FACING)) + 180;
		MatrixTransformStack.of(ms)
			.rotateY(yRot)
			.rotateX(90);
	}

}
