package com.simibubi.create.modules.logistics.block.diodes;

import com.simibubi.create.foundation.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.BlockState;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.math.Vec3d;

public class FlexpeaterScrollSlot extends ValueBoxTransform {

	@Override
	protected Vec3d getLocation(BlockState state) {
		return VecHelper.voxelSpace(8, 3f, 8);
	}

	@Override
	protected Vec3d getOrientation(BlockState state) {
		return new Vec3d(0, AngleHelper.horizontalAngle(state.get(BlockStateProperties.HORIZONTAL_FACING)) + 180, 90);
	}

}
