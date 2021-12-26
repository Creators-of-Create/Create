package com.simibubi.create.compat.jei.display;

import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.components.crusher.AbstractCrushingRecipe;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;

public class MillingDisplay extends AbstractCreateDisplay<AbstractCrushingRecipe> {
	public MillingDisplay(AbstractCrushingRecipe recipe) {
		super(recipe, "milling");
	}
}
