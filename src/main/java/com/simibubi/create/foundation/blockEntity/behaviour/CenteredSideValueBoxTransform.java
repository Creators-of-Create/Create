package com.simibubi.create.foundation.blockEntity.behaviour;

import java.util.function.BiPredicate;

import net.createmod.catnip.utility.VecHelper;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class CenteredSideValueBoxTransform extends ValueBoxTransform.Sided {

	private BiPredicate<BlockState, Direction> allowedDirections;

	public CenteredSideValueBoxTransform() {
		this((b, d) -> true);
	}

	public CenteredSideValueBoxTransform(BiPredicate<BlockState, Direction> allowedDirections) {
		this.allowedDirections = allowedDirections;
	}

	@Override
	protected Vec3 getSouthLocation() {
		return VecHelper.voxelSpace(8, 8, 15.5);
	}

	@Override
	protected boolean isSideActive(BlockState state, Direction direction) {
		return allowedDirections.test(state, direction);
	}

}
