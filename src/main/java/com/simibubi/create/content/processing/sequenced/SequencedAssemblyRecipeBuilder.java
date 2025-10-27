package com.simibubi.create.content.processing.sequenced;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.kinetics.deployer.ItemApplicationRecipeParams;
import com.simibubi.create.content.kinetics.deployer.ItemApplicationRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
import com.simibubi.create.content.processing.recipe.StandardProcessingRecipe;
import com.simibubi.create.content.processing.recipe.StandardProcessingRecipe.Builder;

import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.ItemLike;

import net.neoforged.neoforge.common.conditions.ICondition;

public class SequencedAssemblyRecipeBuilder {

	private ResourceLocation id;
	private SequencedAssemblyRecipe recipe;
	protected List<ICondition> recipeConditions;

	public SequencedAssemblyRecipeBuilder(ResourceLocation id) {
		this.id = id;
		recipeConditions = new ArrayList<>();
		this.recipe = new SequencedAssemblyRecipe(AllRecipeTypes.SEQUENCED_ASSEMBLY.getSerializer());
	}

	public <R extends StandardProcessingRecipe<?>> SequencedAssemblyRecipeBuilder addStep(
		StandardProcessingRecipe.Factory<R> factory,
		UnaryOperator<Builder<R>> builder) {
		return addStep((Function<ResourceLocation, Builder<R>>)
			id -> new Builder<>(factory, id), builder);
	}

	public <R extends ItemApplicationRecipe> SequencedAssemblyRecipeBuilder addStep(
		ItemApplicationRecipe.Factory<R> factory,
		UnaryOperator<ItemApplicationRecipe.Builder<R>> builder) {
		return addStep((Function<ResourceLocation, ItemApplicationRecipe.Builder<R>>)
			id -> new ItemApplicationRecipe.Builder<>(factory, id), builder);
	}

	public <B extends ProcessingRecipeBuilder<?, ?, B>> SequencedAssemblyRecipeBuilder addStep(
		Function<ResourceLocation, B> factory,
		UnaryOperator<B> builder) {
		B recipeBuilder = factory.apply(ResourceLocation.withDefaultNamespace("dummy"));
		Item placeHolder = recipe.getTransitionalItem().getItem();
		recipe.getSequence()
			.add(new SequencedRecipe<>(builder.apply(recipeBuilder.require(placeHolder)
					.output(placeHolder))
				.build()));
		return this;
	}

	public SequencedAssemblyRecipeBuilder require(ItemLike ingredient) {
		return require(Ingredient.of(ingredient));
	}

	public SequencedAssemblyRecipeBuilder require(TagKey<Item> tag) {
		return require(Ingredient.of(tag));
	}

	public SequencedAssemblyRecipeBuilder require(Ingredient ingredient) {
		recipe.ingredient = ingredient;
		return this;
	}

	public SequencedAssemblyRecipeBuilder transitionTo(ItemLike item) {
		recipe.transitionalItem = new ProcessingOutput(item.asItem(), 1, 1);
		return this;
	}

	public SequencedAssemblyRecipeBuilder loops(int loops) {
		recipe.loops = loops;
		return this;
	}

	public SequencedAssemblyRecipeBuilder addOutput(ItemLike item, float weight) {
		return addOutput(new ItemStack(item), weight);
	}

	public SequencedAssemblyRecipeBuilder addOutput(ItemStack item, float weight) {
		recipe.resultPool.add(new ProcessingOutput(item.getItem(), item.getCount(), item.getComponentsPatch(), weight));
		return this;
	}

	public RecipeHolder<SequencedAssemblyRecipe> build() {
		return new RecipeHolder<>(id, recipe);
	}

	public void build(RecipeOutput consumer) {
		RecipeHolder<SequencedAssemblyRecipe> holder = build();

		ResourceLocation id = ResourceLocation.fromNamespaceAndPath(holder.id().getNamespace(),
				AllRecipeTypes.SEQUENCED_ASSEMBLY.getId().getPath() + "/" + holder.id().getPath());

		consumer.accept(id, holder.value(), null, recipeConditions.toArray(new ICondition[0]));
	}
}
