package com.simibubi.create.compat.jei.display;

import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.components.crusher.AbstractCrushingRecipe;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;

public class CrushingDisplay extends AbstractCreateDisplay<AbstractCrushingRecipe> {
	public CrushingDisplay(AbstractCrushingRecipe recipe) {
		super(recipe);
	}

	@Override
	public CategoryIdentifier<?> getCategoryIdentifier() {
		return CategoryIdentifier.of(Create.asResource("crushing"));
	}
}
