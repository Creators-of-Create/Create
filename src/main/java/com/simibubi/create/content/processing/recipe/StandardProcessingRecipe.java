package com.simibubi.create.content.processing.recipe;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.ParametersAreNonnullByDefault;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;

@ParametersAreNonnullByDefault
public abstract class StandardProcessingRecipe<T extends RecipeInput> extends ProcessingRecipe<T, ProcessingRecipeParams> {
	public StandardProcessingRecipe(IRecipeTypeInfo typeInfo, ProcessingRecipeParams params) {
		super(typeInfo, params);
	}

	@FunctionalInterface
	public interface Factory<R extends StandardProcessingRecipe<?>> extends ProcessingRecipe.Factory<ProcessingRecipeParams, R> {
		R create(ProcessingRecipeParams params);
	}

	public static class Builder<R extends StandardProcessingRecipe<?>>
		extends ProcessingRecipeBuilder<ProcessingRecipeParams, R, Builder<R>> {

		public Builder(Factory<R> factory, Identifier recipeId) {
			super(factory, recipeId);
		}

		@Override
		protected ProcessingRecipeParams createParams() {
			return new ProcessingRecipeParams();
		}

		@Override
		public Builder<R> self() {
			return this;
		}
	}

	public static class Serializer {
		private static final Map<RecipeSerializer<?>, Factory<?>> FACTORIES = new ConcurrentHashMap<>();

		public static <R extends StandardProcessingRecipe<?>> RecipeSerializer<R> create(Factory<R> factory) {
			RecipeSerializer<R> serializer = new RecipeSerializer<>(
				ProcessingRecipe.codec(factory, ProcessingRecipeParams.CODEC),
				ProcessingRecipe.streamCodec(factory, ProcessingRecipeParams.STREAM_CODEC)
			);
			FACTORIES.put(serializer, factory);
			return serializer;
		}

		public static boolean hasFactory(RecipeSerializer<?> serializer) {
			return FACTORIES.containsKey(serializer);
		}

		@SuppressWarnings("unchecked")
		public static <R extends StandardProcessingRecipe<?>> Factory<R> factory(RecipeSerializer<?> serializer) {
			Factory<?> factory = FACTORIES.get(serializer);
			if (factory == null)
				throw new IllegalStateException("Recipe serializer " + serializer + " was not created by Create's processing recipe serializer factory");
			return (Factory<R>) factory;
		}
	}
}
