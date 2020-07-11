package com.simibubi.create.content.contraptions.components.fan;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.contraptions.processing.ProcessingIngredient;
import com.simibubi.create.content.contraptions.processing.ProcessingOutput;
import com.simibubi.create.content.contraptions.processing.ProcessingRecipe;
import com.simibubi.create.content.logistics.InWorldProcessing;
import com.simibubi.create.content.logistics.InWorldProcessing.SplashingInv;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
public class SplashingRecipe extends ProcessingRecipe<InWorldProcessing.SplashingInv> {

	public SplashingRecipe(ResourceLocation id, String group, List<ProcessingIngredient> ingredients,
		List<ProcessingOutput> results, int processingDuration) {
		super(AllRecipeTypes.SPLASHING, id, group, ingredients, results, processingDuration);
	}

	@Override
	public boolean matches(SplashingInv inv, World worldIn) {
		if (inv.isEmpty())
			return false;
		return ingredients.get(0)
			.test(inv.getStackInSlot(0));
	}

	@Override
	protected int getMaxOutputCount() {
		return 6;
	}

}
