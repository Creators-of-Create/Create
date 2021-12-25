package com.simibubi.create.compat.jei.display;

import me.shedaniel.rei.api.common.display.Display;
import net.minecraft.world.item.crafting.Recipe;

public abstract class AbstractCreateDisplay<R extends Recipe<?>> implements Display {
	protected final R recipe;

	public AbstractCreateDisplay(R recipe) {
		this.recipe = recipe;
	}

	public R getRecipe() {
		return recipe;
	}
}
