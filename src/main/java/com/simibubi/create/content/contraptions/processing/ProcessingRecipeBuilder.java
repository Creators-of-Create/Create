package com.simibubi.create.content.contraptions.processing;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.simibubi.create.foundation.fluid.FluidHelper;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import com.simibubi.create.foundation.utility.Pair;
import com.simibubi.create.foundation.utility.recipe.IRecipeTypeInfo;

import net.minecraft.data.IFinishedRecipe;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.tags.ITag;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.ModLoadedCondition;
import net.minecraftforge.common.crafting.conditions.NotCondition;
import net.minecraftforge.fluids.FluidStack;

public class ProcessingRecipeBuilder<T extends ProcessingRecipe<?>> {

	protected ProcessingRecipeFactory<T> factory;
	protected ProcessingRecipeParams params;
	protected List<ICondition> recipeConditions;

	public ProcessingRecipeBuilder(ProcessingRecipeFactory<T> factory, ResourceLocation recipeId) {
		params = new ProcessingRecipeParams(recipeId);
		recipeConditions = new ArrayList<>();
		this.factory = factory;
	}

	public ProcessingRecipeBuilder<T> withItemIngredients(Ingredient... ingredients) {
		return withItemIngredients(NonNullList.of(Ingredient.EMPTY, ingredients));
	}

	public ProcessingRecipeBuilder<T> withItemIngredients(NonNullList<Ingredient> ingredients) {
		params.ingredients = ingredients;
		return this;
	}

	public ProcessingRecipeBuilder<T> withSingleItemOutput(ItemStack output) {
		return withItemOutputs(new ProcessingOutput(output, 1));
	}

	public ProcessingRecipeBuilder<T> withItemOutputs(ProcessingOutput... outputs) {
		return withItemOutputs(NonNullList.of(ProcessingOutput.EMPTY, outputs));
	}

	public ProcessingRecipeBuilder<T> withItemOutputs(NonNullList<ProcessingOutput> outputs) {
		params.results = outputs;
		return this;
	}

	public ProcessingRecipeBuilder<T> withFluidIngredients(FluidIngredient... ingredients) {
		return withFluidIngredients(NonNullList.of(FluidIngredient.EMPTY, ingredients));
	}

	public ProcessingRecipeBuilder<T> withFluidIngredients(NonNullList<FluidIngredient> ingredients) {
		params.fluidIngredients = ingredients;
		return this;
	}

	public ProcessingRecipeBuilder<T> withFluidOutputs(FluidStack... outputs) {
		return withFluidOutputs(NonNullList.of(FluidStack.EMPTY, outputs));
	}

	public ProcessingRecipeBuilder<T> withFluidOutputs(NonNullList<FluidStack> outputs) {
		params.fluidResults = outputs;
		return this;
	}

	public ProcessingRecipeBuilder<T> duration(int ticks) {
		params.processingDuration = ticks;
		return this;
	}

	public ProcessingRecipeBuilder<T> averageProcessingDuration() {
		return duration(100);
	}

	public ProcessingRecipeBuilder<T> requiresHeat(HeatCondition condition) {
		params.requiredHeat = condition;
		return this;
	}

	public T build() {
		return factory.create(params);
	}

	public void build(Consumer<IFinishedRecipe> consumer) {
		consumer.accept(new DataGenResult<>(build(), recipeConditions));
	}

	// Datagen shortcuts

	public ProcessingRecipeBuilder<T> require(ITag.INamedTag<Item> tag) {
		return require(Ingredient.of(tag));
	}

	public ProcessingRecipeBuilder<T> require(IItemProvider item) {
		return require(Ingredient.of(item));
	}

	public ProcessingRecipeBuilder<T> require(Ingredient ingredient) {
		params.ingredients.add(ingredient);
		return this;
	}

	public ProcessingRecipeBuilder<T> require(Fluid fluid, int amount) {
		return require(FluidIngredient.fromFluid(fluid, amount));
	}

	public ProcessingRecipeBuilder<T> require(ITag.INamedTag<Fluid> fluidTag, int amount) {
		return require(FluidIngredient.fromTag(fluidTag, amount));
	}

	public ProcessingRecipeBuilder<T> require(FluidIngredient ingredient) {
		params.fluidIngredients.add(ingredient);
		return this;
	}

	public ProcessingRecipeBuilder<T> output(IItemProvider item) {
		return output(item, 1);
	}

	public ProcessingRecipeBuilder<T> output(float chance, IItemProvider item) {
		return output(chance, item, 1);
	}

	public ProcessingRecipeBuilder<T> output(IItemProvider item, int amount) {
		return output(1, item, amount);
	}

	public ProcessingRecipeBuilder<T> output(float chance, IItemProvider item, int amount) {
		return output(chance, new ItemStack(item, amount));
	}

	public ProcessingRecipeBuilder<T> output(ItemStack output) {
		return output(1, output);
	}

	public ProcessingRecipeBuilder<T> output(float chance, ItemStack output) {
		params.results.add(new ProcessingOutput(output, chance));
		return this;
	}

	public ProcessingRecipeBuilder<T> output(float chance, ResourceLocation registryName, int amount) {
		params.results.add(new ProcessingOutput(Pair.of(registryName, amount), chance));
		return this;
	}

	public ProcessingRecipeBuilder<T> output(Fluid fluid, int amount) {
		fluid = FluidHelper.convertToStill(fluid);
		return output(new FluidStack(fluid, amount));
	}

	public ProcessingRecipeBuilder<T> output(FluidStack fluidStack) {
		params.fluidResults.add(fluidStack);
		return this;
	}

	//

	public ProcessingRecipeBuilder<T> whenModLoaded(String modid) {
		return withCondition(new ModLoadedCondition(modid));
	}

	public ProcessingRecipeBuilder<T> whenModMissing(String modid) {
		return withCondition(new NotCondition(new ModLoadedCondition(modid)));
	}

	public ProcessingRecipeBuilder<T> withCondition(ICondition condition) {
		recipeConditions.add(condition);
		return this;
	}

	@FunctionalInterface
	public interface ProcessingRecipeFactory<T extends ProcessingRecipe<?>> {
		T create(ProcessingRecipeParams params);
	}

	public static class ProcessingRecipeParams {

		protected ResourceLocation id;
		protected NonNullList<Ingredient> ingredients;
		protected NonNullList<ProcessingOutput> results;
		protected NonNullList<FluidIngredient> fluidIngredients;
		protected NonNullList<FluidStack> fluidResults;
		protected int processingDuration;
		protected HeatCondition requiredHeat;

		protected ProcessingRecipeParams(ResourceLocation id) {
			this.id = id;
			ingredients = NonNullList.create();
			results = NonNullList.create();
			fluidIngredients = NonNullList.create();
			fluidResults = NonNullList.create();
			processingDuration = 0;
			requiredHeat = HeatCondition.NONE;
		}

	}

	public static class DataGenResult<S extends ProcessingRecipe<?>> implements IFinishedRecipe {

		private List<ICondition> recipeConditions;
		private ProcessingRecipeSerializer<S> serializer;
		private ResourceLocation id;
		private S recipe;

		@SuppressWarnings("unchecked")
		public DataGenResult(S recipe, List<ICondition> recipeConditions) {
			this.recipe = recipe;
			this.recipeConditions = recipeConditions;
			IRecipeTypeInfo recipeType = this.recipe.getTypeInfo();
			ResourceLocation typeId = recipeType.getId();

			if (!(recipeType.getSerializer() instanceof ProcessingRecipeSerializer))
				throw new IllegalStateException("Cannot datagen ProcessingRecipe of type: " + typeId);

			this.id = new ResourceLocation(recipe.getId().getNamespace(),
					typeId.getPath() + "/" + recipe.getId().getPath());
			this.serializer = (ProcessingRecipeSerializer<S>) recipe.getSerializer();
		}

		@Override
		public void serializeRecipeData(JsonObject json) {
			serializer.write(json, recipe);
			if (recipeConditions.isEmpty())
				return;

			JsonArray conds = new JsonArray();
			recipeConditions.forEach(c -> conds.add(CraftingHelper.serialize(c)));
			json.add("conditions", conds);
		}

		@Override
		public ResourceLocation getId() {
			return id;
		}

		@Override
		public IRecipeSerializer<?> getType() {
			return serializer;
		}

		@Override
		public JsonObject serializeAdvancement() {
			return null;
		}

		@Override
		public ResourceLocation getAdvancementId() {
			return null;
		}

	}

}
