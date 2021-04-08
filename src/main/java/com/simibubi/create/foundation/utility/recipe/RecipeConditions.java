package com.simibubi.create.foundation.utility.recipe;

import java.util.function.Predicate;

import com.simibubi.create.foundation.tileEntity.behaviour.filtering.FilteringBehaviour;

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

	public static Predicate<IRecipe<?>> isOfType(IRecipeType<?>... otherTypes) {
		return recipe -> {
			IRecipeType<?> recipeType = recipe.getType();
			for (IRecipeType<?> other : otherTypes)
				if (recipeType == other)
					return true;
			return false;
		};
	}

	public static Predicate<IRecipe<?>> firstIngredientMatches(ItemStack stack) {
		return r -> !r.getIngredients().isEmpty() && r.getIngredients().get(0).test(stack);
	}

	public static Predicate<IRecipe<?>> outputMatchesFilter(FilteringBehaviour filtering) {
		return r -> filtering.test(r.getRecipeOutput());

	}

}
