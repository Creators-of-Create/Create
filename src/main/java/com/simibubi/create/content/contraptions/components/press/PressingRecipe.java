package com.simibubi.create.content.contraptions.components.press;

import javax.annotation.ParametersAreNonnullByDefault;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.contraptions.components.press.MechanicalPressTileEntity.PressingInv;
import com.simibubi.create.content.contraptions.processing.ProcessingRecipe;
import com.simibubi.create.content.contraptions.processing.ProcessingRecipeBuilder.ProcessingRecipeParams;

import net.minecraft.world.World;

@ParametersAreNonnullByDefault
public class PressingRecipe extends ProcessingRecipe<MechanicalPressTileEntity.PressingInv> {

	public PressingRecipe(ProcessingRecipeParams params) {
		super(AllRecipeTypes.PRESSING, params);
	}

	@Override
	public boolean matches(PressingInv inv, World worldIn) {
		if (inv.isEmpty())
			return false;
		return ingredients.get(0)
			.test(inv.getStackInSlot(0));
	}

	@Override
	protected int getMaxInputCount() {
		return 1;
	}
	
	@Override
	protected int getMaxOutputCount() {
		return 2;
	}
}
