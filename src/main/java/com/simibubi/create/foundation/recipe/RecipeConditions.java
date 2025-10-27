package com.simibubi.create.foundation.recipe;

import java.util.function.Predicate;

import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;

/**
 * Commonly used Predicates for searching through recipe collections.
 *
 * @author simibubi
 *
 */
public class RecipeConditions {

	public static Predicate<RecipeHolder<? extends Recipe<?>>> isOfType(RecipeType<?>... otherTypes) {
		return recipe -> {
			RecipeType<?> recipeType = recipe.value().getType();
			for (RecipeType<?> other : otherTypes)
				if (recipeType == other)
					return true;
			return false;
		};
	}

	public static Predicate<RecipeHolder<? extends Recipe<?>>> firstIngredientMatches(ItemStack stack) {
		return r -> !r.value().getIngredients().isEmpty() && r.value().getIngredients().get(0).test(stack);
	}

	public static Predicate<RecipeHolder<? extends Recipe<?>>> outputMatchesFilter(FilteringBehaviour filtering) {
		return r -> filtering.test(r.value().getResultItem(filtering.getWorld().registryAccess()));

	}

}
