package com.simibubi.create.compat.rei.display;

import java.util.Collections;
import java.util.List;

import com.simibubi.create.Create;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.world.item.crafting.Recipe;

public class CreateDisplay<R extends Recipe<?>> implements Display {
	protected final R recipe;
	private final CategoryIdentifier<CreateDisplay<R>> uid;
	private final List<EntryIngredient> input;
	private final List<EntryIngredient> output;

	public CreateDisplay(R recipe, CategoryIdentifier<CreateDisplay<R>> id) {
		this.recipe = recipe;
		this.uid = id;
		this.input = EntryIngredients.ofIngredients(recipe.getIngredients());
		this.output = Collections.singletonList(EntryIngredients.of(recipe.getResultItem()));
	}

	public R getRecipe() {
		return recipe;
	}

	@Override
	public List<EntryIngredient> getInputEntries() {
		return input;
	}

	@Override
	public List<EntryIngredient> getOutputEntries() {
		return output;
	}

	@Override
	public CategoryIdentifier<?> getCategoryIdentifier() {
		return uid;
	}
}
