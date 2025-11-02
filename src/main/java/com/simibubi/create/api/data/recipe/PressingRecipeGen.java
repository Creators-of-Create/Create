package com.simibubi.create.api.data.recipe;

import java.util.concurrent.CompletableFuture;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.kinetics.press.PressingRecipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;

/**
 * The base class for Pressing recipe generation.
 * Addons should extend this and use the {@link ProcessingRecipeGen#create} methods
 * to make recipes.
 * For an example of how you might do this, see Create's implementation: {@link com.simibubi.create.foundation.data.recipe.CreatePressingRecipeGen}.
 * Needs to be added to a registered recipe provider to do anything, see {@link com.simibubi.create.foundation.data.recipe.CreateRecipeProvider}
 */
public abstract class PressingRecipeGen extends StandardProcessingRecipeGen<PressingRecipe> {

	protected GeneratedRecipe moddedCompacting(DatagenMod mod, String input, String output) {
		return create("compat/" + mod.getId() + "/" + output, b -> b.require(mod, input)
			.output(mod, output)
			.whenModLoaded(mod.getId()));
	}

	protected GeneratedRecipe moddedPaths(DatagenMod mod, String... blocks) {
		for (String block : blocks) {
			moddedCompacting(mod, block, block + "_path");
		}
		return null;
	}

	public PressingRecipeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, String defaultNamespace) {
		super(output, registries, defaultNamespace);
	}

	@Override
	protected AllRecipeTypes getRecipeType() {
		return AllRecipeTypes.PRESSING;
	}

}
