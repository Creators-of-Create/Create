package com.simibubi.create.content.processing.recipe;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.annotation.ParametersAreNonnullByDefault;

import com.google.common.base.Joiner;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class ProcessingRecipe<I extends RecipeInput, P extends ProcessingRecipeParams> implements Recipe<I> {

	protected P params;
	protected NonNullList<Ingredient> ingredients;
	protected NonNullList<ProcessingOutput> results;
	protected NonNullList<SizedFluidIngredient> fluidIngredients;
	protected NonNullList<FluidStack> fluidResults;
	protected int processingDuration;
	protected HeatCondition requiredHeat;

	private RecipeType<?> type;
	private RecipeSerializer<?> serializer;
	private IRecipeTypeInfo typeInfo;
	private Supplier<ItemStack> forcedResult;

	public ProcessingRecipe(IRecipeTypeInfo typeInfo, P params) {
		this.params = params;
		this.ingredients = params.ingredients;
		this.fluidIngredients = params.fluidIngredients;
		this.results = params.results;
		this.fluidResults = params.fluidResults;
		this.processingDuration = params.processingDuration;
		this.requiredHeat = params.requiredHeat;
		this.type = typeInfo.getType();
		this.serializer = typeInfo.getSerializer();
		this.typeInfo = typeInfo;
		this.forcedResult = null;
	}

	// Recipe type options:

	protected abstract int getMaxInputCount();

	protected abstract int getMaxOutputCount();

	protected boolean canRequireHeat() {
		return false;
	}

	protected boolean canSpecifyDuration() {
		return false;
	}

	protected int getMaxFluidInputCount() {
		return 0;
	}

	protected int getMaxFluidOutputCount() {
		return 0;
	}

	public List<String> validate() {
		List<String> errors = new ArrayList<>();
		int ingredientCount = ingredients.size();
		int outputCount = results.size();

		if (ingredientCount > getMaxInputCount())
			errors.add("Recipe has more item inputs (" + ingredientCount + ") than supported ("
				+ getMaxInputCount() + ").");

		if (outputCount > getMaxOutputCount())
			errors.add("Recipe has more item outputs (" + outputCount + ") than supported ("
				+ getMaxOutputCount() + ").");

		ingredientCount = fluidIngredients.size();
		outputCount = fluidResults.size();

		if (ingredientCount > getMaxFluidInputCount())
			errors.add("Recipe has more fluid inputs (" + ingredientCount + ") than supported ("
						+ getMaxFluidInputCount() + ").");

		if (outputCount > getMaxFluidOutputCount())
			errors.add("Recipe has more fluid outputs (" + outputCount + ") than supported ("
						+ getMaxFluidOutputCount() + ").");

		if (processingDuration > 0 && !canSpecifyDuration())
			errors.add("Recipe specified a duration. Durations have no impact on this type of recipe.");

		if (requiredHeat != HeatCondition.NONE && !canRequireHeat())
			errors.add("Recipe specified a heat condition. Heat conditions have no impact on this type of recipe.");

		return errors;
	}

	public P getParams() {
		return params;
	}

	@Override
	public NonNullList<Ingredient> getIngredients() {
		return ingredients;
	}

	public NonNullList<SizedFluidIngredient> getFluidIngredients() {
		return fluidIngredients;
	}

	public List<ProcessingOutput> getRollableResults() {
		return results;
	}

	public NonNullList<FluidStack> getFluidResults() {
		return fluidResults;
	}

	public List<ItemStack> getRollableResultsAsItemStacks() {
		return getRollableResults().stream()
			.map(ProcessingOutput::getStack)
			.collect(Collectors.toList());
	}

	public void enforceNextResult(Supplier<ItemStack> stack) {
		forcedResult = stack;
	}

	public List<ItemStack> rollResults(RandomSource randomSource) {
		return rollResults(this.getRollableResults(), randomSource);
	}

	public List<ItemStack> rollResults(List<ProcessingOutput> rollableResults, RandomSource randomSource) {
		List<ItemStack> results = new ArrayList<>();
		for (int i = 0; i < rollableResults.size(); i++) {
			ProcessingOutput output = rollableResults.get(i);
			ItemStack stack = i == 0 && forcedResult != null ? forcedResult.get() : output.rollOutput(randomSource);
			if (!stack.isEmpty())
				results.add(stack);
		}
		return results;
	}

	public int getProcessingDuration() {
		return processingDuration;
	}

	public HeatCondition getRequiredHeat() {
		return requiredHeat;
	}

	// IRecipe<> paperwork

	@Override
	public ItemStack assemble(I t, HolderLookup.Provider provider) {
		return getResultItem(provider);
	}

	@Override
	public boolean canCraftInDimensions(int width, int height) {
		return true;
	}

	@Override
	public ItemStack getResultItem(HolderLookup.Provider provider) {
		return getRollableResults().isEmpty() ? ItemStack.EMPTY
				: getRollableResults().getFirst()
				.getStack();
	}

	@Override
	public boolean isSpecial() {
		return true;
	}

	// Processing recipes do not show up in the recipe book
	@Override
	public String getGroup() {
		return "processing";
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return serializer;
	}

	@Override
	public RecipeType<?> getType() {
		return type;
	}

	public IRecipeTypeInfo getTypeInfo() {
		return typeInfo;
	}

	public static <P extends ProcessingRecipeParams, R extends ProcessingRecipe<?, P>> MapCodec<R> codec(
		Factory<P, R> factory, MapCodec<P> paramsCodec
	) {
		return paramsCodec.xmap(factory::create, recipe -> recipe.getParams())
			.validate(recipe -> {
				var errors = recipe.validate();
				if (errors.isEmpty())
					return DataResult.success(recipe);
				errors.add(recipe.getClass().getSimpleName() + " failed validation:");
				return DataResult.error(() -> Joiner.on('\n').join(errors), recipe);
			});
	}

	public static <P extends ProcessingRecipeParams, R extends ProcessingRecipe<?, P>> StreamCodec<RegistryFriendlyByteBuf, R> streamCodec(
		Factory<P, R> factory, StreamCodec<RegistryFriendlyByteBuf, P> streamCodec
	) {
		return streamCodec.map(factory::create, ProcessingRecipe::getParams);
	}

	@FunctionalInterface
	public interface Factory<P extends ProcessingRecipeParams, R extends ProcessingRecipe<?, P>> {
		R create(P params);
	}
}
