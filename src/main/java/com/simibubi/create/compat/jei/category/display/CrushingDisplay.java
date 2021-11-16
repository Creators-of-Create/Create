package com.simibubi.create.compat.jei.category.display;

import com.simibubi.create.content.contraptions.components.crusher.AbstractCrushingRecipe;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;

import java.util.List;

public class CrushingDisplay extends AbstractCreateDisplay<AbstractCrushingRecipe> {
	public CrushingDisplay(AbstractCrushingRecipe recipe) {
		super(recipe);
	}

	@Override
	public List<EntryIngredient> getInputEntries() {
		return null;
	}

	@Override
	public List<EntryIngredient> getOutputEntries() {
		return null;
	}

	@Override
	public CategoryIdentifier<?> getCategoryIdentifier() {
		return null;
	}
}
