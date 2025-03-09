package com.simibubi.create.api.data.recipe;

import com.simibubi.create.AllRecipeTypes;

import net.minecraft.data.PackOutput;

public class CompactingRecipeGen extends ProcessingRecipeGen {

	public CompactingRecipeGen(PackOutput generator, String defaultNamespace) {
		super(generator, defaultNamespace);
	}

	@Override
	protected AllRecipeTypes getRecipeType() {
		return AllRecipeTypes.COMPACTING;
	}

}
