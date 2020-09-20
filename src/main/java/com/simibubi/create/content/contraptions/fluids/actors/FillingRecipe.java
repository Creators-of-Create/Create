package com.simibubi.create.content.contraptions.fluids.actors;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.contraptions.processing.ProcessingRecipe;
import com.simibubi.create.content.contraptions.processing.ProcessingRecipeBuilder.ProcessingRecipeParams;
import com.simibubi.create.foundation.fluid.FluidIngredient;

import net.minecraft.world.World;
import net.minecraftforge.items.wrapper.RecipeWrapper;

public class FillingRecipe extends ProcessingRecipe<RecipeWrapper> {

	public FillingRecipe(ProcessingRecipeParams params) {
		super(AllRecipeTypes.FILLING, params);
	}

	@Override
	public boolean matches(RecipeWrapper inv, World p_77569_2_) {
		return ingredients.get(0).test(inv.getStackInSlot(0));
	}

	@Override
	protected int getMaxInputCount() {
		return 1;
	}

	@Override
	protected int getMaxOutputCount() {
		return 1;
	}
	
	@Override
	protected int getMaxFluidInputCount() {
		return 1;
	}
	
	public FluidIngredient getRequiredFluid() {
		if (fluidIngredients.isEmpty())
			throw new IllegalStateException("Filling Recipe: " + id.toString() + " has no fluid ingredient!");
		return fluidIngredients.get(0);
	}

}
