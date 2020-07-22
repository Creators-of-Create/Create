package com.simibubi.create.content.contraptions.components.press;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.contraptions.components.press.MechanicalPressTileEntity.PressingInv;
import com.simibubi.create.content.contraptions.processing.ProcessingIngredient;
import com.simibubi.create.content.contraptions.processing.ProcessingOutput;
import com.simibubi.create.content.contraptions.processing.ProcessingRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
public class PressingRecipe extends ProcessingRecipe<MechanicalPressTileEntity.PressingInv> {

	public PressingRecipe(ResourceLocation id, String group, List<ProcessingIngredient> ingredients,
		List<ProcessingOutput> results, int processingDuration) {
		super(AllRecipeTypes.PRESSING, id, group, ingredients, results, processingDuration);
	}

	@Override
	public boolean matches(PressingInv inv, World worldIn) {
		if (inv.isEmpty())
			return false;
		return ingredients.get(0)
			.test(inv.getStackInSlot(0));
	}

	@Override
	protected int getMaxOutputCount() {
		return 2;
	}
}
