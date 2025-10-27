package com.simibubi.create.foundation.recipe.trie;

import java.util.Set;

import net.minecraft.world.item.crafting.Recipe;

public class AbstractRecipe<R extends Recipe<?>> {
	final R recipe;
	final Set<AbstractIngredient> ingredients;

	public AbstractRecipe(R recipe, Set<AbstractIngredient> ingredients) {
		this.recipe = recipe;
		this.ingredients = ingredients;
	}
}
