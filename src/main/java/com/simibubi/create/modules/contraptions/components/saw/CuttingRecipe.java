package com.simibubi.create.modules.contraptions.components.saw;

import java.util.List;

import com.simibubi.create.AllRecipes;
import com.simibubi.create.modules.contraptions.processing.ProcessingInventory;
import com.simibubi.create.modules.contraptions.processing.ProcessingRecipe;
import com.simibubi.create.modules.contraptions.processing.StochasticOutput;

import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class CuttingRecipe extends ProcessingRecipe<ProcessingInventory> {

	public CuttingRecipe(ResourceLocation id, String group, List<Ingredient> ingredients,
			List<StochasticOutput> results, int processingDuration) {
		super(AllRecipes.CUTTING, id, group, ingredients, results, processingDuration);
	}

	@Override
	public boolean matches(ProcessingInventory inv, World worldIn) {
		if (inv.isEmpty())
			return false;
		return ingredients.get(0).test(inv.getStackInSlot(0));
	}

}
