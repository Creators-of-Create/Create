package com.simibubi.create.content.contraptions.components.mixer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.contraptions.processing.BasinTileEntity.BasinInputInventory;
import com.simibubi.create.content.contraptions.processing.ProcessingRecipe;
import com.simibubi.create.content.contraptions.processing.ProcessingRecipeBuilder;
import com.simibubi.create.content.contraptions.processing.ProcessingRecipeBuilder.ProcessingRecipeParams;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;

public class MixingRecipe extends ProcessingRecipe<BasinInputInventory> {

	/**
	 * For JEI purposes only
	 */
	public static MixingRecipe convertShapeless(IRecipe<?> recipe) {
		return new ProcessingRecipeBuilder<>(MixingRecipe::new, recipe.getId())
			.withItemIngredients(recipe.getIngredients())
			.withSingleItemOutput(recipe.getRecipeOutput())
			.build();
	}

	public MixingRecipe(ProcessingRecipeParams params) {
		super(AllRecipeTypes.MIXING, params);
	}

	@Override
	protected int getMaxInputCount() {
		return 9;
	}

	@Override
	protected int getMaxOutputCount() {
		return 1;// TODO increase
	}

	@Override
	protected int getMaxFluidInputCount() {
		return 2;
	}

	@Override
	protected int getMaxFluidOutputCount() {
		return 1;// TODO increase?
	}
	
	@Override
	protected boolean canRequireHeat() {
		return true;
	}

	@Override
	public boolean matches(BasinInputInventory inv, @Nonnull World worldIn) {
		if (inv.isEmpty())
			return false;

		NonNullList<Ingredient> ingredients = this.getIngredients();
		if (!ingredients.stream()
			.allMatch(Ingredient::isSimple))
			return false;

		List<ItemStack> remaining = new ArrayList<>();
		for (int slot = 0; slot < inv.getSizeInventory(); ++slot) {
			ItemStack itemstack = inv.getStackInSlot(slot);
			if (!itemstack.isEmpty()) {
				remaining.add(itemstack.copy());
			}
		}

		// sort by leniency
		List<Ingredient> sortedIngredients = new LinkedList<>(ingredients);
		sortedIngredients.sort(Comparator.comparingInt(i -> i.getMatchingStacks().length));
		Ingredients: for (Ingredient ingredient : sortedIngredients) {
			for (ItemStack stack : remaining) {
				if (stack.isEmpty())
					continue;
				if (ingredient.test(stack)) {
					stack.shrink(1);
					continue Ingredients;
				}
			}
			return false;
		}
		return true;
	}

}
