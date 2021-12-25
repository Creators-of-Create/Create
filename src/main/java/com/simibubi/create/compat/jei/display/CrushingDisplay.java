package com.simibubi.create.compat.jei.display;

import java.util.List;

import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.components.crusher.AbstractCrushingRecipe;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import me.shedaniel.rei.api.common.util.EntryIngredients;

public class CrushingDisplay extends AbstractCreateDisplay<AbstractCrushingRecipe> {
	public CrushingDisplay(AbstractCrushingRecipe recipe) {
		super(recipe);
	}

	@Override
	public List<EntryIngredient> getInputEntries() {
		return EntryIngredients.ofIngredients(recipe.getIngredients());
	}

	@Override
	public List<EntryIngredient> getOutputEntries() {
		return List.of(EntryIngredient.of(EntryStack.of(VanillaEntryTypes.ITEM, recipe.getResultItem())));
	}

	@Override
	public CategoryIdentifier<?> getCategoryIdentifier() {
		return CategoryIdentifier.of(Create.asResource("crushing"));
	}


}
