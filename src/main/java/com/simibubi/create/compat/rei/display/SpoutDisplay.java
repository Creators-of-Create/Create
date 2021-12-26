package com.simibubi.create.compat.rei.display;

import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.fluids.actors.FillingRecipe;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;

public class SpoutDisplay extends AbstractCreateDisplay<FillingRecipe> {
	public SpoutDisplay(FillingRecipe recipe) {
		super(recipe, "spout_filling");
	}

	@Override
	public CategoryIdentifier<?> getCategoryIdentifier() {
		return CategoryIdentifier.of(Create.asResource("spout_filling"));
	}
}
