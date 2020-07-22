package com.simibubi.create.foundation.tileEntity.behaviour;

import java.util.function.BiPredicate;

import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3d;

public class CenteredSideValueBoxTransform extends ValueBoxTransform.Sided {

	private BiPredicate<BlockState, Direction> allowedDirections;

	public CenteredSideValueBoxTransform() {
		this((b, d) -> true);
	}
	
	public CenteredSideValueBoxTransform(BiPredicate<BlockState, Direction> allowedDirections) {
		this.allowedDirections = allowedDirections;
	}

	@Override
	protected Vector3d getSouthLocation() {
		return VecHelper.voxelSpace(8, 8, 16);
	}

	@Override
	protected boolean isSideActive(BlockState state, Direction direction) {
		return allowedDirections.test(state, direction);
	}

}
