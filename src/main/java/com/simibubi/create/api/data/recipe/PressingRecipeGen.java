package com.simibubi.create.api.data.recipe;

import com.simibubi.create.AllRecipeTypes;

import net.minecraft.data.PackOutput;

public abstract class PressingRecipeGen extends ProcessingRecipeGen {

	public PressingRecipeGen(PackOutput output, String defaultNamespace) {
		super(output, defaultNamespace);
	}

	@Override
	protected AllRecipeTypes getRecipeType() {
		return AllRecipeTypes.PRESSING;
	}

}
