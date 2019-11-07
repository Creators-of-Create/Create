package com.simibubi.create.modules.contraptions.receivers;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.simibubi.create.AllRecipes;
import com.simibubi.create.modules.contraptions.base.ProcessingRecipe;
import com.simibubi.create.modules.contraptions.base.StochasticOutput;
import com.simibubi.create.modules.contraptions.receivers.BasinTileEntity.BasinInputInventory;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class MixingRecipe extends ProcessingRecipe<BasinInputInventory> {

	public MixingRecipe(ResourceLocation id, String group, List<Ingredient> ingredients,
			List<StochasticOutput> results, int processingDuration) {
		super(AllRecipes.MIXING, id, group, ingredients, results, processingDuration);
	}

	@Override
	public boolean matches(BasinInputInventory inv, World worldIn) {
		if (inv.isEmpty())
			return false;
		
		NonNullList<Ingredient> ingredients = getIngredients();
		if (!ingredients.stream().allMatch(Ingredient::isSimple))
			return false;

		List<ItemStack> remaining = new ArrayList<>();
		for (int slot = 0; slot < inv.getSizeInventory(); ++slot) {
			ItemStack itemstack = inv.getStackInSlot(slot);
			if (!itemstack.isEmpty()) {
				remaining.add(itemstack.copy());
			}
		}

		// sort by leniency
		List<Ingredient> sortedIngredients = new LinkedList<>(ingredients);
		sortedIngredients.sort((i1, i2) -> i1.getMatchingStacks().length - i2.getMatchingStacks().length);
		Ingredients: for (Ingredient ingredient : sortedIngredients) {
			for (ItemStack stack : remaining) {
				if (stack.isEmpty())
					continue;
				if (ingredient.test(stack)) {
					stack.shrink(1);
					continue Ingredients;
				}
			}
			return false;
		}
		return true;
		
	}

}
