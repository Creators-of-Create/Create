package com.simibubi.create.foundation.recipe;

import java.util.function.Predicate;

import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;

/**
 * Commonly used Predicates for searching through recipe collections.
 * 
 * @author simibubi
 *
 */
public class RecipeConditions {

	public static Predicate<Recipe<?>> isOfType(RecipeType<?>... otherTypes) {
		return recipe -> {
			RecipeType<?> recipeType = recipe.getType();
			for (RecipeType<?> other : otherTypes)
				if (recipeType == other)
					return true;
			return false;
		};
	}

	public static Predicate<Recipe<?>> firstIngredientMatches(ItemStack stack) {
		return r -> !r.getIngredients().isEmpty() && r.getIngredients().get(0).test(stack);
	}

	public static Predicate<Recipe<?>> outputMatchesFilter(FilteringBehaviour filtering) {
		return r -> filtering.test(r.getResultItem(filtering.getWorld().registryAccess()));

	}

}
