package com.simibubi.create.compat.jei.category.display;

import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.fluids.actors.FillingRecipe;

import dev.architectury.fluid.FluidStack;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;

import java.util.ArrayList;
import java.util.List;

public class SpoutDisplay extends AbstractCreateDisplay<FillingRecipe> {
	public SpoutDisplay(FillingRecipe recipe) {
		super(recipe);
	}

	@Override
	public List<EntryIngredient> getInputEntries() {
		return new ArrayList<>();
	}

	@Override
	public List<EntryIngredient> getOutputEntries() {
		return List.of(EntryIngredient.of(EntryStack.of(VanillaEntryTypes.ITEM, recipe.getResultItem())));
	}

	@Override
	public CategoryIdentifier<?> getCategoryIdentifier() {
		return CategoryIdentifier.of(Create.asResource("spout_filling"));
	}
}
