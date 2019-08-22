package com.simibubi.create.modules.contraptions.receivers;

import java.util.List;

import com.simibubi.create.AllRecipes;
import com.simibubi.create.modules.contraptions.base.ProcessingRecipe;
import com.simibubi.create.modules.contraptions.base.StochasticOutput;
import com.simibubi.create.modules.contraptions.receivers.CrushingWheelControllerTileEntity.Inventory;

import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class CrushingRecipe extends ProcessingRecipe<CrushingWheelControllerTileEntity.Inventory> {

	public CrushingRecipe(ResourceLocation id, String group, List<Ingredient> ingredients,
			List<StochasticOutput> results, int processingDuration) {
		super(AllRecipes.Crushing, id, group, ingredients, results, processingDuration);
	}

	@Override
	public boolean matches(Inventory inv, World worldIn) {
		if (inv.isEmpty())
			return false;
		return ingredients.get(0).test(inv.getStackInSlot(0));
	}

}
