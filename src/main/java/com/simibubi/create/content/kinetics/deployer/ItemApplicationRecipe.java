package com.simibubi.create.content.kinetics.deployer;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;

import com.simibubi.create.content.processing.recipe.ProcessingRecipeParams;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import net.neoforged.neoforge.items.wrapper.RecipeWrapper;

public class ItemApplicationRecipe extends ProcessingRecipe<RecipeWrapper, ItemApplicationRecipeParams> {

	private boolean keepHeldItem;

	public ItemApplicationRecipe(AllRecipeTypes type, ItemApplicationRecipeParams params) {
		super(type, params);
		keepHeldItem = params.keepHeldItem;
	}

	@Override
	public boolean matches(RecipeWrapper inv, Level p_77569_2_) {
		return getProcessedItem().test(inv.getItem(0)) && getRequiredHeldItem().test(inv.getItem(1));
	}

	@Override
	protected int getMaxInputCount() {
		return 2;
	}

	@Override
	protected int getMaxOutputCount() {
		return 4;
	}

	public boolean shouldKeepHeldItem() {
		return keepHeldItem;
	}

	public Ingredient getRequiredHeldItem() {
		if (ingredients.size() < 2)
			throw new IllegalStateException("Item Application Recipe has no tool!");
		return ingredients.get(1);
	}

	public Ingredient getProcessedItem() {
		if (ingredients.isEmpty())
			throw new IllegalStateException("Item Application Recipe has no ingredient!");
		return ingredients.get(0);
	}

	@FunctionalInterface
	public interface Factory<R extends ItemApplicationRecipe> extends ProcessingRecipe.Factory<ItemApplicationRecipeParams, R> {
		R create(ItemApplicationRecipeParams params);
	}

	public static class Builder<R extends ItemApplicationRecipe> extends ProcessingRecipeBuilder<ItemApplicationRecipeParams, R, Builder<R>> {
		public Builder(Factory<R> factory, ResourceLocation recipeId) {
			super(factory, recipeId);
		}

		@Override
		protected ItemApplicationRecipeParams createParams() {
			return new ItemApplicationRecipeParams();
		}

		@Override
		public Builder<R> self() {
			return this;
		}

		public Builder<R> toolNotConsumed() {
			params.keepHeldItem = true;
			return this;
		}
	}

	public static class Serializer<R extends ItemApplicationRecipe> implements RecipeSerializer<R> {
		private final MapCodec<R> codec;
		private final StreamCodec<RegistryFriendlyByteBuf, R> streamCodec;

		public Serializer(ProcessingRecipe.Factory<ItemApplicationRecipeParams, R> factory) {
			this.codec = ProcessingRecipe.codec(factory, ItemApplicationRecipeParams.CODEC);
			this.streamCodec = ProcessingRecipe.streamCodec(factory, ItemApplicationRecipeParams.STREAM_CODEC);
		}

		@Override
		public MapCodec<R> codec() {
			return codec;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, R> streamCodec() {
			return streamCodec;
		}

	}
}
