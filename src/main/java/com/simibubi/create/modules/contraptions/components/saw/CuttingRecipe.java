package com.simibubi.create.modules.contraptions.components.saw;

import java.util.List;

import com.simibubi.create.AllRecipes;
import com.simibubi.create.modules.contraptions.processing.ProcessingIngredient;
import com.simibubi.create.modules.contraptions.processing.ProcessingOutput;
import com.simibubi.create.modules.contraptions.processing.ProcessingRecipe;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.items.wrapper.RecipeWrapper;

public class CuttingRecipe extends ProcessingRecipe<RecipeWrapper> {

	public CuttingRecipe(ResourceLocation id, String group, List<ProcessingIngredient> ingredients,
			List<ProcessingOutput> results, int processingDuration) {
		super(AllRecipes.CUTTING, id, group, ingredients, results, processingDuration);
	}

	@Override
	public boolean matches(RecipeWrapper inv, World worldIn) {
		if (inv.isEmpty())
			return false;
		return ingredients.get(0).test(inv.getStackInSlot(0));
	}
	
	@Override
	protected int getMaxOutputCount() {
		return 4;
	}

}
