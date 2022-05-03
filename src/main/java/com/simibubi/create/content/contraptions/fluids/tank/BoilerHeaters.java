package com.simibubi.create.content.contraptions.fluids.tank;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllTags.AllBlockTags;
import com.simibubi.create.content.contraptions.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.contraptions.processing.burner.BlazeBurnerBlock.HeatLevel;

import net.minecraft.world.level.block.state.BlockState;

public class BoilerHeaters { // API?

	public static boolean canHeatPassively(BlockState state) {
		if (AllBlocks.BLAZE_BURNER.has(state))
			return state.getValue(BlazeBurnerBlock.HEAT_LEVEL) != HeatLevel.NONE;
		if (AllBlockTags.PASSIVE_BOILER_HEATERS.matches(state))
			return true;
		return false;
	}

	public static int getActiveHeatOf(BlockState state) {
		if (AllBlocks.BLAZE_BURNER.has(state)) {
			HeatLevel value = state.getValue(BlazeBurnerBlock.HEAT_LEVEL);
			switch (value) {
			case FADING:
			case KINDLED:
				return 1;
			case SEETHING:
				return 2;
			default:
			case SMOULDERING:
			case NONE:
				return 0;
			}
		}
		return 0;

	}

}
