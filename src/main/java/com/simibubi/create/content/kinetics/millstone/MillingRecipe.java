package com.simibubi.create.content.kinetics.millstone;

import javax.annotation.ParametersAreNonnullByDefault;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.kinetics.crusher.AbstractCrushingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeParams;

import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.level.Level;

@ParametersAreNonnullByDefault
public class MillingRecipe extends AbstractCrushingRecipe {

	public MillingRecipe(ProcessingRecipeParams params) {
		super(AllRecipeTypes.MILLING, params);
	}

	@Override
	public boolean matches(RecipeInput inv, Level worldIn) {
		if (inv.isEmpty())
			return false;
		return ingredients.get(0)
			.test(inv.getItem(0));
	}

	@Override
	protected int getMaxOutputCount() {
		return 4;
	}
}
