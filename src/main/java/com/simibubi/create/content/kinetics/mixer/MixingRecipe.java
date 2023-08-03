package com.simibubi.create.content.kinetics.mixer;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.processing.basin.BasinRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder.ProcessingRecipeParams;

public class MixingRecipe extends BasinRecipe {

	public MixingRecipe(ProcessingRecipeParams params) {
		super(AllRecipeTypes.MIXING, params);
	}

	@Override
	protected boolean canSpecifyDuration() {
		return true;
	}
}
