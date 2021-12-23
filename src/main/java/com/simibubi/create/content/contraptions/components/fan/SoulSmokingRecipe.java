package com.simibubi.create.content.contraptions.components.fan;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.contraptions.processing.InWorldProcessing;
import com.simibubi.create.content.contraptions.processing.InWorldProcessing.SplashingWrapper;
import com.simibubi.create.content.contraptions.processing.ProcessingRecipe;
import com.simibubi.create.content.contraptions.processing.ProcessingRecipeBuilder.ProcessingRecipeParams;

import net.minecraft.world.level.Level;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class SoulSmokingRecipe extends ProcessingRecipe<InWorldProcessing.SoulSmokingWrapper> {

	public SoulSmokingRecipe(ProcessingRecipeParams params) {
		super(AllRecipeTypes.SOUL_SMOKING, params);
	}

	@Override
	public boolean matches(InWorldProcessing.SoulSmokingWrapper inv, Level worldIn) {
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
