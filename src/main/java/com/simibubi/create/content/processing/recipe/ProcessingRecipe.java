package com.simibubi.create.content.processing.recipe;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.annotation.ParametersAreNonnullByDefault;

import org.slf4j.Logger;

import com.simibubi.create.Create;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

import net.neoforged.neoforge.fluids.FluidStack;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class ProcessingRecipe<I extends RecipeInput, P extends ProcessingRecipeParams> implements Recipe<I> {

	protected P params;
	protected NonNullList<Ingredient> ingredients;
	protected NonNullList<ProcessingOutput> results;
	protected NonNullList<FluidIngredient> fluidIngredients;
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

	//TODO: Recipe id is no longer avcailable on construct,
	// 		validation should be called in a reload listener after RecipeManager if needed,
	//		currently only validates recipes created from builder for datagen
	public void validate(ResourceLocation id) {
		Logger logger = Create.LOGGER;
		String messageHeader = "Your custom " + typeInfo.getId() + " recipe (" + id + ")";
		int ingredientCount = ingredients.size();
		int outputCount = results.size();

		if (ingredientCount > getMaxInputCount())
			logger.warn(messageHeader + " has more item inputs (" + ingredientCount + ") than supported ("
				+ getMaxInputCount() + ").");

		if (outputCount > getMaxOutputCount())
			logger.warn(messageHeader + " has more item outputs (" + outputCount + ") than supported ("
				+ getMaxOutputCount() + ").");

		ingredientCount = fluidIngredients.size();
		outputCount = fluidResults.size();

		if (ingredientCount > getMaxFluidInputCount())
			logger.warn(messageHeader + " has more fluid inputs (" + ingredientCount + ") than supported ("
						+ getMaxFluidInputCount() + ").");

		if (outputCount > getMaxFluidOutputCount())
			logger.warn(messageHeader + " has more fluid outputs (" + outputCount + ") than supported ("
						+ getMaxFluidOutputCount() + ").");

		if (processingDuration > 0 && !canSpecifyDuration())
			logger.warn(messageHeader + " specified a duration. Durations have no impact on this type of recipe.");

		if (requiredHeat != HeatCondition.NONE && !canRequireHeat())
			logger.warn(
				messageHeader + " specified a heat condition. Heat conditions have no impact on this type of recipe.");
	}

	public P getParams() {
		return params;
	}

	@Override
	public NonNullList<Ingredient> getIngredients() {
		return ingredients;
	}

	public NonNullList<FluidIngredient> getFluidIngredients() {
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

	public List<ItemStack> rollResults() {
		return rollResults(this.getRollableResults());
	}

	public List<ItemStack> rollResults(List<ProcessingOutput> rollableResults) {
		List<ItemStack> results = new ArrayList<>();
		for (int i = 0; i < rollableResults.size(); i++) {
			ProcessingOutput output = rollableResults.get(i);
			ItemStack stack = i == 0 && forcedResult != null ? forcedResult.get() : output.rollOutput();
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

	@FunctionalInterface
	public interface Factory<P extends ProcessingRecipeParams, R extends ProcessingRecipe<?, P>> {
		R create(P params);
	}
}
