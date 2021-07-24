package com.simibubi.create.content.contraptions.components.saw;

import com.jozufozu.flywheel.util.transform.MatrixTransformStack;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.foundation.tileEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3d;

public class SawFilterSlot extends ValueBoxTransform {

	@Override
	protected Vector3d getLocalOffset(BlockState state) {
		if (state.getValue(SawBlock.FACING) != Direction.UP)
			return null;
		Vector3d x = VecHelper.voxelSpace(8f, 12.5f, 12.25f);
		Vector3d z = VecHelper.voxelSpace(12.25f, 12.5f, 8f);
		return state.getValue(SawBlock.AXIS_ALONG_FIRST_COORDINATE) ? z : x;
	}

	@Override
	protected void rotate(BlockState state, MatrixStack ms) {
		int yRot = state.getValue(SawBlock.AXIS_ALONG_FIRST_COORDINATE) ? 270 : 180;
		MatrixTransformStack.of(ms)
			.rotateY(yRot)
			.rotateX(90);
	}

}
