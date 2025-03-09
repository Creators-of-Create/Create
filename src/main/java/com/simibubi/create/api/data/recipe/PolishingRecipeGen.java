package com.simibubi.create.api.data.recipe;

import com.simibubi.create.AllRecipeTypes;

import net.minecraft.data.PackOutput;

public class PolishingRecipeGen extends ProcessingRecipeGen {

	public PolishingRecipeGen(PackOutput output, String defaultNamespace) {
		super(output, defaultNamespace);
	}

	@Override
	protected AllRecipeTypes getRecipeType() {
		return AllRecipeTypes.SANDPAPER_POLISHING;
	}

}
