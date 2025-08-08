package com.simibubi.create.api.data.recipe;

import java.util.concurrent.CompletableFuture;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.kinetics.mixer.MixingRecipe;
import com.simibubi.create.content.processing.basin.BasinRecipe;
import com.simibubi.create.content.processing.basin.BasinRecipe.Builder;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeParams;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

/**
 * The base class for Mixing recipe generation.
 * Addons should extend this and use the {@link ProcessingRecipeGen#create} methods
 * to make recipes.
 * For an example of how you might do this, see Create's implementation: {@link com.simibubi.create.foundation.data.recipe.CreateMixingRecipeGen}.
 * Needs to be added to a registered recipe provider to do anything, see {@link com.simibubi.create.foundation.data.recipe.CreateRecipeProvider}
 */
public abstract class MixingRecipeGen extends ProcessingRecipeGen<ProcessingRecipeParams, MixingRecipe, BasinRecipe.Builder<MixingRecipe>> {

	public MixingRecipeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, String defaultNamespace) {
		super(output, registries, defaultNamespace);
	}

	@Override
	protected AllRecipeTypes getRecipeType() {
		return AllRecipeTypes.MIXING;
	}

	protected BasinRecipe.Serializer<MixingRecipe> getSerializer() {
		return getRecipeType().getSerializer();
	}

	@Override
	protected Builder<MixingRecipe> getBuilder(ResourceLocation id) {
		return new BasinRecipe.Builder<>(getSerializer().factory(), id);
	}
}
