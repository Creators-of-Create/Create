package com.simibubi.create.content.processing.basin;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeParams;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class BasinRecipe extends AbstractBasinRecipe<ProcessingRecipeParams> {

	protected BasinRecipe(IRecipeTypeInfo type, ProcessingRecipeParams params) {
		super(type, params);
	}

	public BasinRecipe(ProcessingRecipeParams params) {
		super(params);
	}

	public static RecipeHolder<BasinRecipe> convertShapeless(RecipeHolder<?> recipe) {
		BasinRecipe basinRecipe =
			new Builder<>(BasinRecipe::new, recipe.id()).withItemIngredients(recipe.value().getIngredients())
				.withSingleItemOutput(recipe.value().getResultItem(Minecraft.getInstance().level.registryAccess()))
				.build();
		return new RecipeHolder<>(recipe.id(), basinRecipe);
	}

	public static class Builder<R extends BasinRecipe> extends ProcessingRecipeBuilder<ProcessingRecipeParams, R, Builder<R>> {

		public Builder(ProcessingRecipe.Factory<ProcessingRecipeParams, R> factory, ResourceLocation recipeId) {
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

	@MethodsReturnNonnullByDefault
	public static class Serializer<R extends BasinRecipe> implements RecipeSerializer<R> {
		private final ProcessingRecipe.Factory<ProcessingRecipeParams, R> factory;
		private final MapCodec<R> codec;
		private final StreamCodec<RegistryFriendlyByteBuf, R> streamCodec;

		public Serializer(ProcessingRecipe.Factory<ProcessingRecipeParams, R> factory) {
			this.factory = factory;
			this.codec = ProcessingRecipe.codec(factory, ProcessingRecipeParams.CODEC);
			this.streamCodec = ProcessingRecipe.streamCodec(factory, ProcessingRecipeParams.STREAM_CODEC);
		}

		@Override
		public MapCodec<R> codec() {
			return codec;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, R> streamCodec() {
			return streamCodec;
		}

		public ProcessingRecipe.Factory<ProcessingRecipeParams, R> factory() {
			return factory;
		}
	}

}
