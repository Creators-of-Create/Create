package com.simibubi.create.foundation.utility;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class BoundingBoxHelper {

	public static void expand(BoundingBox original, BoundingBox expandBy) {
		original.minX = Math.min(original.minX, expandBy.minX);
		original.minY = Math.min(original.minY, expandBy.minY);
		original.minZ = Math.min(original.minZ, expandBy.minZ);
		original.maxX = Math.max(original.maxX, expandBy.maxX);
		original.maxY = Math.max(original.maxY, expandBy.maxY);
		original.maxZ = Math.max(original.maxZ, expandBy.maxZ);
	}

	public static BoundingBox of(BlockPos from, BlockPos to) {
		return new BoundingBox(from.getX(), from.getY(), from.getZ(), to.getX(), to.getY(), to.getZ());
	}
}
