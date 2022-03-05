package com.simibubi.create.content.contraptions.components.crusher;

import com.simibubi.create.content.contraptions.processing.ProcessingRecipe;
import com.simibubi.create.content.contraptions.processing.ProcessingRecipeBuilder.ProcessingRecipeParams;
import com.simibubi.create.foundation.utility.recipe.IRecipeTypeInfo;
import io.github.fabricators_of_create.porting_lib.transfer.item.RecipeWrapper;

public abstract class AbstractCrushingRecipe extends ProcessingRecipe<RecipeWrapper> {

	public AbstractCrushingRecipe(IRecipeTypeInfo recipeType, ProcessingRecipeParams params) {
		super(recipeType, params);
	}

	@Override
	protected int getMaxInputCount() {
		return 1;
	}
}
