package com.simibubi.create.content.contraptions.processing;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.Create;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.stream.Collectors;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class ProcessingRecipe<T extends IInventory> implements IRecipe<T> {
	protected final List<ProcessingIngredient> ingredients;
	protected final ResourceLocation id;
	protected final String group;
	protected final int processingDuration;
	protected final List<FluidStack> fluidIngredients;
	protected final List<FluidStack> fluidResults;
	protected final int requiredHeat;
	private final List<ProcessingOutput> results;
	private final IRecipeType<?> type;
	private final IRecipeSerializer<?> serializer;

	public ProcessingRecipe(AllRecipeTypes recipeType, ResourceLocation id, String group,
		List<ProcessingIngredient> ingredients, List<ProcessingOutput> results, int processingDuration) {
		this(recipeType, id, group, ingredients, results, processingDuration, null, null, 0);
	}

	public ProcessingRecipe(AllRecipeTypes recipeType, ResourceLocation id, String group,
		List<ProcessingIngredient> ingredients, List<ProcessingOutput> results, int processingDuration,
		@Nullable List<FluidStack> fluidIngredients, @Nullable List<FluidStack> fluidResults, int requiredHeat) {
		this.type = recipeType.type;
		this.serializer = recipeType.serializer;
		this.id = id;
		this.group = group;
		this.ingredients = ingredients;
		this.results = results;
		this.processingDuration = processingDuration;
		this.fluidIngredients = fluidIngredients;
		this.fluidResults = fluidResults;
		this.requiredHeat = requiredHeat;
		validate(recipeType);
	}

	private void validate(AllRecipeTypes recipeType) {
		if (ingredients.size() > getMaxInputCount())
			Create.logger.warn("Your custom " + recipeType.name() + " recipe (" + id.toString() + ") has more inputs ("
				+ ingredients.size() + ") than supported (" + getMaxInputCount() + ").");
		if (results.size() > getMaxOutputCount())
			Create.logger.warn("Your custom " + recipeType.name() + " recipe (" + id.toString() + ") has more outputs ("
				+ results.size() + ") than supported (" + getMaxOutputCount() + ").");
		ingredients.forEach(i -> {
			if (i.isCatalyst() && !canHaveCatalysts())
				Create.logger.warn("Your custom " + recipeType.name() + " recipe (" + id.toString()
					+ ") has a catalyst ingredient, which act like a regular ingredient in this type.");
		});
	}

	@Override
	public NonNullList<Ingredient> getIngredients() {
		NonNullList<Ingredient> nonnulllist = NonNullList.create();
		this.ingredients.forEach(e -> nonnulllist.add(e.getIngredient()));
		return nonnulllist;
	}

	public int getProcessingDuration() {
		return processingDuration;
	}

	public CombinedItemFluidList rollResults() {
		CombinedItemFluidList results = new CombinedItemFluidList();
		for (ProcessingOutput output : getRollableItemResults()) {
			ItemStack stack = output.rollOutput();
			if (!stack.isEmpty())
				results.add(stack);
		}
		return results;
	}

	@Override
	public ItemStack getCraftingResult(T inv) {
		return getRecipeOutput();
	}

	@Override
	public boolean canFit(int width, int height) {
		return true;
	}

	@Override
	public ItemStack getRecipeOutput() {
		return getRollableItemResults().isEmpty() ? ItemStack.EMPTY
			: getRollableItemResults().get(0)
				.getStack();
	}

	@Override
	public ResourceLocation getId() {
		return id;
	}

	@Override
	public IRecipeSerializer<?> getSerializer() {
		return serializer;
	}

	@Override
	public String getGroup() {
		return group;
	}

	@Override
	public IRecipeType<?> getType() {
		return type;
	}

	protected int getMaxInputCount() {
		return 1;
	}

	protected int getMaxOutputCount() {
		return 15;
	}

	protected boolean canHaveCatalysts() {
		return false;
	}

	public List<ProcessingOutput> getRollableItemResults() {
		return results;
	}

	public List<ProcessingIngredient> getRollableIngredients() {
		return ingredients;
	}

	public List<ItemStack> getPossibleOutputs() {
		return getRollableItemResults().stream()
			.map(ProcessingOutput::getStack)
			.collect(Collectors.toList());
	}

	protected boolean canHaveFluidIngredient() {
		return false;
	}

	protected boolean canHaveFluidOutput() {
		return false;
	}

	protected boolean requiresHeating() {
		return false;
	}
}
