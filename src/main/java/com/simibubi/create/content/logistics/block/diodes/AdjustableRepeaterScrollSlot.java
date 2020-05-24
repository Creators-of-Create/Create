package com.simibubi.create.content.logistics.block.diodes;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.foundation.tileEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.BlockState;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.math.Vec3d;

public class AdjustableRepeaterScrollSlot extends ValueBoxTransform {

	@Override
	protected Vec3d getLocalOffset(BlockState state) {
		return VecHelper.voxelSpace(8, 3f, 8);
	}

	@Override
	protected void rotate(BlockState state, MatrixStack ms) {
		float y = AngleHelper.horizontalAngle(state.get(BlockStateProperties.HORIZONTAL_FACING)) + 180;
		ms.multiply(VecHelper.rotateY(y));
		ms.multiply(VecHelper.rotateX(90));
	}

}
