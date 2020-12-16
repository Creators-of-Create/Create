package com.simibubi.create.foundation.utility.placement;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.vector.Vector3i;

import java.util.function.Function;

public class PlacementOffset {

	private final boolean success;
	private final Vector3i pos;
	private final Function<BlockState, BlockState> stateTransform;

	private PlacementOffset(boolean success, Vector3i pos, Function<BlockState, BlockState> transform) {
		this.success = success;
		this.pos = pos;
		this.stateTransform = transform == null ? Function.identity() : transform;
	}

	public static PlacementOffset fail() {
		return new PlacementOffset(false, Vector3i.NULL_VECTOR, null);
	}

	public static PlacementOffset success(Vector3i pos) {
		return new PlacementOffset(true, pos, null);
	}

	public static PlacementOffset success(Vector3i pos, Function<BlockState, BlockState> transform) {
		return new PlacementOffset(true, pos, transform);
	}

	public boolean isSuccessful() {
		return success;
	}

	public Vector3i getPos() {
		return pos;
	}

	public Function<BlockState, BlockState> getTransform() {
		return stateTransform;
	}
}
