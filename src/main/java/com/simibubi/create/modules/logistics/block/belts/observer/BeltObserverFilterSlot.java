package com.simibubi.create.modules.logistics.block.belts.observer;

import com.simibubi.create.foundation.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.util.math.Vec3d;

public class BeltObserverFilterSlot extends ValueBoxTransform {

	Vec3d position = VecHelper.voxelSpace(8f, 14.5f, 16f);
	
	@Override
	protected Vec3d getLocation(BlockState state) {
		return rotateHorizontally(state, position);
	}

	@Override
	protected Vec3d getOrientation(BlockState state) {
		float yRot = AngleHelper.horizontalAngle(state.get(HorizontalBlock.HORIZONTAL_FACING));
		return new Vec3d(0, 180 + yRot, 90);
	}

}
