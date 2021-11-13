package com.simibubi.create.lib.utility;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class BoundingBoxUtil {
	public static BoundingBox from2BlockPos(BlockPos one, BlockPos two) {
		return new BoundingBox(
				one.getX(),
				one.getY(),
				one.getZ(),
				two.getX(),
				two.getY(),
				two.getZ()
		);
	}

	public static BoundingBox expandFromOtherBox(BoundingBox first, BoundingBox second) {
		int minX = Math.min(first.minX(), second.minX());
		int minY = Math.min(first.minY(), second.minY());
		int minZ = Math.min(first.minZ(), second.minZ());
		int maxX = Math.max(first.maxX(), second.maxX());
		int maxY = Math.max(first.maxY(), second.maxY());
		int maxZ = Math.max(first.maxZ(), second.maxZ());
		return new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
	}
}
