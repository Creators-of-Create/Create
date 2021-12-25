package com.simibubi.create.compat.jei.display;

import com.simibubi.create.content.contraptions.processing.BasinRecipe;

// Is this class even necessary? Couldn't we just have MixingDisplay and PackingDisplay classes instead?
public abstract class BasinDisplay extends AbstractCreateDisplay<BasinRecipe> {
	public BasinDisplay(BasinRecipe recipe) {
		super(recipe);
	}
}
