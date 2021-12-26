package com.simibubi.create.compat.jei.display;

import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.components.saw.CuttingRecipe;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;

public class SawingDisplay extends AbstractCreateDisplay<CuttingRecipe> {
	public SawingDisplay(CuttingRecipe recipe) {
		super(recipe);
	}

	@Override
	public CategoryIdentifier<?> getCategoryIdentifier() {
		return CategoryIdentifier.of(Create.asResource("sawing"));
	}
}
