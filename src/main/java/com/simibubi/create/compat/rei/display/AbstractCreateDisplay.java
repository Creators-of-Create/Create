package com.simibubi.create.compat.rei.display;

import java.util.Collections;
import java.util.List;

import com.simibubi.create.Create;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.world.item.crafting.Recipe;

public abstract class AbstractCreateDisplay<R extends Recipe<?>> implements Display {
	protected final R recipe;
	private final CategoryIdentifier uid;

	public AbstractCreateDisplay(R recipe, String id) {
		this.recipe = recipe;
		this.uid = CategoryIdentifier.of(Create.asResource(id));
	}

	public R getRecipe() {
		return recipe;
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
		return uid;
	}
}
