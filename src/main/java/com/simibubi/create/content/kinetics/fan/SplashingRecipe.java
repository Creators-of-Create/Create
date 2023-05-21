package com.simibubi.create.content.kinetics.fan;

import javax.annotation.ParametersAreNonnullByDefault;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.kinetics.fan.FanProcessing.SplashingWrapper;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder.ProcessingRecipeParams;

import net.minecraft.world.level.Level;

@ParametersAreNonnullByDefault
public class SplashingRecipe extends ProcessingRecipe<FanProcessing.SplashingWrapper> {

	public SplashingRecipe(ProcessingRecipeParams params) {
		super(AllRecipeTypes.SPLASHING, params);
	}

	@Override
	public boolean matches(SplashingWrapper inv, Level worldIn) {
		if (inv.isEmpty())
			return false;
		return ingredients.get(0)
			.test(inv.getItem(0));
	}

	@Override
	protected int getMaxInputCount() {
		return 1;
	}

	@Override
	protected int getMaxOutputCount() {
		return 12;
	}

}
