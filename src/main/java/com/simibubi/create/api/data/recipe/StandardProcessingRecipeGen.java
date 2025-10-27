package com.simibubi.create.api.data.recipe;

import java.util.concurrent.CompletableFuture;

import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeParams;
import com.simibubi.create.content.processing.recipe.StandardProcessingRecipe;

import com.simibubi.create.content.processing.recipe.StandardProcessingRecipe.Builder;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

/**
 * A base class for {@link StandardProcessingRecipe}, containing helper methods
 * for datagenning processing recipes.
 * <p>
 * Addons should extend this for custom processing recipe that extends {@link StandardProcessingRecipe},
 * and return the recipe type in {@link #getRecipeType()}.
 */
public abstract class StandardProcessingRecipeGen<R extends StandardProcessingRecipe<?>> extends ProcessingRecipeGen<ProcessingRecipeParams, R, StandardProcessingRecipe.Builder<R>> {
	public StandardProcessingRecipeGen(PackOutput output, CompletableFuture<Provider> registries, String defaultNamespace) {
		super(output, registries, defaultNamespace);
	}

	protected StandardProcessingRecipe.Serializer<R> getSerializer() {
		return getRecipeType().getSerializer();
	}

	@Override
	protected Builder<R> getBuilder(ResourceLocation id) {
		return new StandardProcessingRecipe.Builder<>(getSerializer().factory(), id);
	}
}
