package com.simibubi.create.compat.rei.display;

import com.simibubi.create.content.contraptions.processing.BasinRecipe;

public class MixingDisplay extends BasinDisplay {
	public MixingDisplay(BasinRecipe recipe) {
		super(recipe, "mixing");
	}

	private MixingDisplay(BasinRecipe recipe, String id) {
		super(recipe, "mixing");
	}

	public static MixingDisplay shapeless(BasinRecipe recipe) {
		return new MixingDisplay(recipe, "automatic_shapeless");
	}

	public static MixingDisplay autoBrewing(BasinRecipe recipe) {
		return new MixingDisplay(recipe, "automatic_brewing");
	}
}
