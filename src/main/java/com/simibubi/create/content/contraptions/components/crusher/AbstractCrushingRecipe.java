package com.simibubi.create.content.contraptions.components.crusher;

import java.util.List;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.contraptions.processing.ProcessingIngredient;
import com.simibubi.create.content.contraptions.processing.ProcessingOutput;
import com.simibubi.create.content.contraptions.processing.ProcessingRecipe;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.wrapper.RecipeWrapper;

public abstract class AbstractCrushingRecipe extends ProcessingRecipe<RecipeWrapper> {

	public AbstractCrushingRecipe(AllRecipeTypes recipeType, ResourceLocation id, String group,
		List<ProcessingIngredient> ingredients, List<ProcessingOutput> results, int processingDuration) {
		super(recipeType, id, group, ingredients, results, processingDuration);
	}

}
