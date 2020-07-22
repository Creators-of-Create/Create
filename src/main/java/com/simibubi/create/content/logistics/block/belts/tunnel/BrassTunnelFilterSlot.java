package com.simibubi.create.content.logistics.block.belts.tunnel;

import com.simibubi.create.foundation.tileEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.util.math.vector.Vector3d;

public class BrassTunnelFilterSlot extends ValueBoxTransform.Sided {

	@Override
	protected Vector3d getSouthLocation() {
		return VecHelper.voxelSpace(8, 13, 15.5f);
	}

}
