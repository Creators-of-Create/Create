package com.simibubi.create.lib.util;

import com.simibubi.create.lib.mixin.accessor.StairBlockAccessor;

import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockState;

public class StairsBlockHelper {
	public static StairBlock init(BlockState blockState, Properties properties) {
		return StairBlockAccessor.create$init(blockState, properties);
	}
}
