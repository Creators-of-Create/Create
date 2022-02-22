package com.simibubi.create.content.contraptions.fluids.tank;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllTags.AllBlockTags;
import com.simibubi.create.content.contraptions.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.contraptions.processing.burner.BlazeBurnerBlock.HeatLevel;

import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.state.BlockState;

public class BoilerHeaters { // API?

	public static float getAddedHeatOf(BlockState state) {
		if (AllBlocks.BLAZE_BURNER.has(state)) {
			HeatLevel value = state.getValue(BlazeBurnerBlock.HEAT_LEVEL);
			switch (value) {
			case SMOULDERING:
				return 24;
			case FADING:
			case KINDLED:
				return 96;
			case SEETHING:
				return 32 * 8;
			default:
			case NONE:
				return 0;
			}
		}
		if (state.hasProperty(AbstractFurnaceBlock.LIT) && !state.getValue(AbstractFurnaceBlock.LIT))
			return 0;
		if (AllBlockTags.ACTIVE_BOILER_HEATERS.matches(state))
			return 48;
		if (AllBlockTags.PASSIVE_BOILER_HEATERS.matches(state))
			return 12;
		return 0;

	}

}
