package com.simibubi.create.foundation.data.recipe;

import java.util.concurrent.CompletableFuture;

import com.simibubi.create.content.processing.recipe.StandardProcessingRecipe;
import com.simibubi.create.content.processing.recipe.StandardProcessingRecipe.Builder;
import com.simibubi.create.content.processing.recipe.StandardProcessingRecipe.Serializer;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

public abstract class StandardProcessingRecipeGen<R extends StandardProcessingRecipe<?>> extends ProcessingRecipeGen<Builder<R>> {
	public StandardProcessingRecipeGen(PackOutput generator, CompletableFuture<Provider> registries) {
		super(generator, registries);
	}

	@Override
	protected Builder<R> getBuilder(ResourceLocation id) {
		return new Builder<>(getRecipeType().<Serializer<R>>getSerializer().factory(), id);
	}
}
