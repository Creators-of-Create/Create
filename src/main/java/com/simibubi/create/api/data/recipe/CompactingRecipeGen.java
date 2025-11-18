package com.simibubi.create.api.data.recipe;

import java.util.concurrent.CompletableFuture;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.kinetics.mixer.CompactingRecipe;
import com.simibubi.create.content.processing.basin.BasinRecipe;
import com.simibubi.create.content.processing.basin.BasinRecipe.Builder;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeParams;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

/**
 * The base class for Compacting recipe generation.
 * Addons should extend this and use the {@link ProcessingRecipeGen#create} methods
 * to make recipes.
 * For an example of how you might do this, see Create's implementation: {@link com.simibubi.create.foundation.data.recipe.CreateCompactingRecipeGen}.
 * Needs to be added to a registered recipe provider to do anything, see {@link com.simibubi.create.foundation.data.recipe.CreateRecipeProvider}
 */
public abstract class CompactingRecipeGen extends ProcessingRecipeGen<ProcessingRecipeParams, CompactingRecipe, BasinRecipe.Builder<CompactingRecipe>> {

	public CompactingRecipeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, String defaultNamespace) {
		super(output, registries, defaultNamespace);
	}

	@Override
	protected AllRecipeTypes getRecipeType() {
		return AllRecipeTypes.COMPACTING;
	}

	protected BasinRecipe.Serializer<CompactingRecipe> getSerializer() {
		return getRecipeType().getSerializer();
	}

	@Override
	protected Builder<CompactingRecipe> getBuilder(ResourceLocation id) {
		return new BasinRecipe.Builder<>(getSerializer().factory(), id);
	}
}
