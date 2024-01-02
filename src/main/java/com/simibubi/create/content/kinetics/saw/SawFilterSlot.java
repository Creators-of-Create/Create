package com.simibubi.create.content.kinetics.saw;

import com.jozufozu.flywheel.lib.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class SawFilterSlot extends ValueBoxTransform {

	@Override
	public Vec3 getLocalOffset(BlockState state) {
		if (state.getValue(SawBlock.FACING) != Direction.UP)
			return null;
		int offset = state.getValue(SawBlock.FLIPPED) ? -3 : 3;
		Vec3 x = VecHelper.voxelSpace(8, 12.5f, 8 + offset);
		Vec3 z = VecHelper.voxelSpace(8 + offset, 12.5f, 8);
		return state.getValue(SawBlock.AXIS_ALONG_FIRST_COORDINATE) ? z : x;
	}

	@Override
	public void rotate(BlockState state, PoseStack ms) {
		int yRot = (state.getValue(SawBlock.AXIS_ALONG_FIRST_COORDINATE) ? 90 : 0)
			+ (state.getValue(SawBlock.FLIPPED) ? 0 : 180);
		TransformStack.of(ms)
			.rotateY(yRot)
			.rotateX(90);
	}

}
