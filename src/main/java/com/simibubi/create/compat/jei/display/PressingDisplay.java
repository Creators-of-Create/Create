package com.simibubi.create.compat.jei.display;

import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.components.press.PressingRecipe;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;

public class PressingDisplay extends AbstractCreateDisplay<PressingRecipe> {
	public PressingDisplay(PressingRecipe recipe) {
		super(recipe);
	}

	@Override
	public CategoryIdentifier<?> getCategoryIdentifier() {
		return CategoryIdentifier.of(Create.asResource("pressing"));
	}
}
