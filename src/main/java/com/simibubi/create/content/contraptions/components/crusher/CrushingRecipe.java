package com.simibubi.create.content.contraptions.components.crusher;

import javax.annotation.ParametersAreNonnullByDefault;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.contraptions.processing.ProcessingRecipeBuilder.ProcessingRecipeParams;

import net.minecraft.world.level.Level;
import com.simibubi.create.lib.transfer.item.RecipeWrapper;

@ParametersAreNonnullByDefault
public class CrushingRecipe extends AbstractCrushingRecipe {

	public CrushingRecipe(ProcessingRecipeParams params) {
		super(AllRecipeTypes.CRUSHING, params);
	}

	@Override
	public boolean matches(RecipeWrapper inv, Level worldIn) {
		if (inv.isEmpty())
			return false;
		return ingredients.get(0)
			.test(inv.getItem(0));
	}

	@Override
	protected int getMaxOutputCount() {
		return 7;
	}

}
