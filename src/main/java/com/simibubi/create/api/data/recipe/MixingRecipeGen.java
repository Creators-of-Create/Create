package com.simibubi.create.api.data.recipe;

import com.simibubi.create.AllRecipeTypes;

import net.minecraft.data.PackOutput;
import net.minecraft.world.level.material.Fluids;

/**
 * The base class for Mixing recipe generation.
 * Addons should extend this and use the {@link ProcessingRecipeGen#create} methods
 * to make recipes.
 * For an example of how you might do this, see Create's implementation: {@link com.simibubi.create.foundation.data.recipe.CreateMixingRecipeGen}.
 * Needs to be added to a registered recipe provider to do anything, see {@link com.simibubi.create.foundation.data.recipe.CreateRecipeProvider}
 */
public abstract class MixingRecipeGen extends ProcessingRecipeGen {

	protected GeneratedRecipe moddedMud(DatagenMod mod, String name) {
		String mud = name + "_mud";
		return create(mod.recipeId(mud), b -> b.require(Fluids.WATER, 250)
			.require(mod, name + "_dirt")
			.output(mod, mud)
			.whenModLoaded(mod.getId()));
	}

	public MixingRecipeGen(PackOutput output, String defaultNamespace) {
		super(output, defaultNamespace);
	}

	@Override
	protected AllRecipeTypes getRecipeType() {
		return AllRecipeTypes.MIXING;
	}

}
