package com.simibubi.create.compat.jei.display;

import com.simibubi.create.Create;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import net.minecraft.world.item.crafting.CraftingRecipe;

public class MechanicalCraftingDisplay extends AbstractCreateDisplay<CraftingRecipe> {
	public MechanicalCraftingDisplay(CraftingRecipe recipe) {
		super(recipe);
	}

	@Override
	public List<EntryIngredient> getInputEntries() {
		return EntryIngredients.ofIngredients(recipe.getIngredients());
	}

	@Override
	public List<EntryIngredient> getOutputEntries() {
		return Collections.singletonList(EntryIngredients.of(recipe.getResultItem()));
	}

	@Override
	public CategoryIdentifier<?> getCategoryIdentifier() {
		return CategoryIdentifier.of(Create.asResource("mechanical_crafting"));
	}
}
