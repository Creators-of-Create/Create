package com.simibubi.create.modules.contraptions.processing;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.simibubi.create.AllRecipes;
import com.simibubi.create.Create;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

public abstract class ProcessingRecipe<T extends IInventory> implements IRecipe<T> {
	protected final List<ProcessingIngredient> ingredients;
	private final List<ProcessingOutput> results;
	private final IRecipeType<?> type;
	private final IRecipeSerializer<?> serializer;
	protected final ResourceLocation id;
	protected final String group;
	protected final int processingDuration;

	public ProcessingRecipe(AllRecipes recipeType, ResourceLocation id, String group,
			List<ProcessingIngredient> ingredients, List<ProcessingOutput> results, int processingDuration) {
		this.type = recipeType.type;
		this.serializer = recipeType.serializer;
		this.id = id;
		this.group = group;
		this.ingredients = ingredients;
		this.results = results;
		this.processingDuration = processingDuration;
		validate(recipeType);
	}

	private void validate(AllRecipes recipeType) {
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

	public List<ItemStack> rollResults() {
		List<ItemStack> stacks = new ArrayList<>();
		for (ProcessingOutput output : getRollableResults()) {
			ItemStack stack = output.rollOutput();
			if (!stack.isEmpty())
				stacks.add(stack);
		}
		return stacks;
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
		return getRollableResults().isEmpty() ? ItemStack.EMPTY : getRollableResults().get(0).getStack();
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

	public List<ProcessingOutput> getRollableResults() {
		return results;
	}
	
	public List<ProcessingIngredient> getRollableIngredients() {
		return ingredients;
	}

	public List<ItemStack> getPossibleOutputs() {
		return getRollableResults().stream().map(output -> output.getStack()).collect(Collectors.toList());
	}
}
