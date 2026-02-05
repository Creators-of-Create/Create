package com.simibubi.create.content.processing.recipe;

import com.simibubi.create.api.recipe.HeatCondition;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class EmptyHeatCondition implements HeatCondition {
	@Override
	public boolean test(Level level, BlockPos testPos) {
		return true;
	}

	@Override
	public String getTranslationKey() {
		return "create.recipe.heat_requirement.none";
	}
}
