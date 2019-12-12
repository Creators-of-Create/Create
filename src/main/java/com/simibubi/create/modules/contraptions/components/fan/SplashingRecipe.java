package com.simibubi.create.modules.contraptions.components.fan;

import java.util.List;

import com.simibubi.create.AllRecipes;
import com.simibubi.create.modules.contraptions.processing.ProcessingRecipe;
import com.simibubi.create.modules.contraptions.processing.StochasticOutput;
import com.simibubi.create.modules.logistics.InWorldProcessing;
import com.simibubi.create.modules.logistics.InWorldProcessing.SplashingInv;

import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class SplashingRecipe extends ProcessingRecipe<InWorldProcessing.SplashingInv> {

	public SplashingRecipe(ResourceLocation id, String group, List<Ingredient> ingredients,
			List<StochasticOutput> results, int processingDuration) {
		super(AllRecipes.SPLASHING, id, group, ingredients, results, processingDuration);
	}

	@Override
	public boolean matches(SplashingInv inv, World worldIn) {
		if (inv.isEmpty())
			return false;
		return ingredients.get(0).test(inv.getStackInSlot(0));
	}

}
