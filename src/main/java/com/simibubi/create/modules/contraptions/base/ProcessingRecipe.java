package com.simibubi.create.modules.contraptions.base;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.AllRecipes;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

public abstract class ProcessingRecipe<T extends IInventory> implements IRecipe<T> {
	protected final List<Ingredient> ingredients;
	protected final List<StochasticOutput> results;
	private final IRecipeType<?> type;
	private final IRecipeSerializer<?> serializer;
	protected final ResourceLocation id;
	protected final String group;
	protected final int processingDuration;

	public ProcessingRecipe(AllRecipes recipeType, ResourceLocation id, String group,
			List<Ingredient> ingredients, List<StochasticOutput> results, int processingDuration) {
		this.type = recipeType.type;
		this.serializer = recipeType.serializer;
		this.id = id;
		this.group = group;
		this.ingredients = ingredients;
		this.results = results;
		this.processingDuration = processingDuration;
	}

	@Override
	public NonNullList<Ingredient> getIngredients() {
		NonNullList<Ingredient> nonnulllist = NonNullList.create();
		nonnulllist.addAll(this.ingredients);
		return nonnulllist;
	}
	
	public int getProcessingDuration() {
		return processingDuration;
	}
	
	public List<StochasticOutput> getAllResults() {
		return results;
	}
	
	public List<ItemStack> rollResults() {
		List<ItemStack> stacks = new ArrayList<>();
		for (StochasticOutput output : results) {
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
		return results.isEmpty() ? ItemStack.EMPTY : results.get(0).getStack();
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
}
