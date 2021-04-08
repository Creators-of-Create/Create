package com.simibubi.create.foundation.utility;

import net.minecraft.util.math.BlockPos;

@FunctionalInterface
public interface Coordinate {
	float get(BlockPos from);
}
