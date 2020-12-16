package com.simibubi.create.foundation.utility.placement;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.Vec3i;

import java.util.function.Function;

public class PlacementOffset {

	private final boolean success;
	private final Vec3i pos;
	private final Function<BlockState, BlockState> stateTransform;

	private PlacementOffset(boolean success, Vec3i pos, Function<BlockState, BlockState> transform) {
		this.success = success;
		this.pos = pos;
		this.stateTransform = transform == null ? Function.identity() : transform;
	}

	public static PlacementOffset fail() {
		return new PlacementOffset(false, Vec3i.NULL_VECTOR, null);
	}

	public static PlacementOffset success(Vec3i pos) {
		return new PlacementOffset(true, pos, null);
	}

	public static PlacementOffset success(Vec3i pos, Function<BlockState, BlockState> transform) {
		return new PlacementOffset(true, pos, transform);
	}

	public boolean isSuccessful() {
		return success;
	}

	public Vec3i getPos() {
		return pos;
	}

	public Function<BlockState, BlockState> getTransform() {
		return stateTransform;
	}
}
