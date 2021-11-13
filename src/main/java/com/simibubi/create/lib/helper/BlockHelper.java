package com.simibubi.create.lib.helper;

import com.simibubi.create.lib.mixin.accessor.BlockAccessor;
import com.simibubi.create.lib.utility.MixinHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;

public class BlockHelper {
	public static void dropXpOnBlockBreak(Block block, ServerLevel serverWorld, BlockPos blockPos, int i) {
		 get(block).create$popExperience(serverWorld, blockPos, i);
	}

	private static BlockAccessor get(Block block) {
		return MixinHelper.cast(block);
	}

	private BlockHelper() {}
}
