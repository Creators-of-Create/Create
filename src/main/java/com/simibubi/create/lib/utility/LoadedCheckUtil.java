package com.simibubi.create.lib.utility;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;

public class LoadedCheckUtil {
	public static boolean isAreaLoaded(LevelAccessor world, BlockPos center, int range) {
		return world.hasChunksAt(center.offset(-range, -range, -range), center.offset(range, range, range));
	}
}
