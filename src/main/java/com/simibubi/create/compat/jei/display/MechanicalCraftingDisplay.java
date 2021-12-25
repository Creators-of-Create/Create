package com.simibubi.create.compat.jei.display;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryIngredient;

import java.util.List;

import net.minecraft.world.item.crafting.CraftingRecipe;

public class MechanicalCraftingDisplay extends AbstractCreateDisplay<CraftingRecipe> {
	public MechanicalCraftingDisplay(CraftingRecipe recipe) {
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
