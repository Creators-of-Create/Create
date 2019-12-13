package com.simibubi.create.foundation.utility.recipe;

import com.google.common.base.Predicate;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;

/**
 * Commonly used Predicates for searching through recipe collections.
 * 
 * @author simibubi
 *
 */
public class RecipeConditions {

	public static Predicate<IRecipe<?>> isOfType(IRecipeType<?> type, IRecipeType<?>... otherTypes) {
		return recipe -> {
			IRecipeType<?> recipeType = recipe.getType();
			if (recipeType == type)
				return true;
			for (IRecipeType<?> other : otherTypes)
				if (recipeType == other)
					return true;
			return false;
		};
	}

	public static Predicate<IRecipe<?>> firstIngredientMatches(ItemStack stack) {
		return r -> !r.getIngredients().isEmpty() && r.getIngredients().get(0).test(stack);
	}

	public static Predicate<IRecipe<?>> outputMatchesFilter(ItemStack filter) {
		return r -> filter.isEmpty() || ItemStack.areItemsEqual(filter, r.getRecipeOutput());
	}

}
