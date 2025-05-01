package com.simibubi.create.api.data.recipe;

import com.simibubi.create.AllRecipeTypes;

import net.minecraft.data.PackOutput;

/**
 * The base class for Compacting recipe generation.
 * Addons should extend this and use the {@link ProcessingRecipeGen#create} methods
 * to make recipes.
 * For an example of how you might do this, see Create's implementation: {@link com.simibubi.create.foundation.data.recipe.CreateCompactingRecipeGen}.
 * Needs to be added to a registered recipe provider to do anything, see {@link com.simibubi.create.foundation.data.recipe.CreateRecipeProvider}
 */
public abstract class CompactingRecipeGen extends ProcessingRecipeGen {

	public CompactingRecipeGen(PackOutput generator, String defaultNamespace) {
		super(generator, defaultNamespace);
	}

	@Override
	protected AllRecipeTypes getRecipeType() {
		return AllRecipeTypes.COMPACTING;
	}

}
