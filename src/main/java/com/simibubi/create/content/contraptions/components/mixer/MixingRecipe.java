package com.simibubi.create.content.contraptions.components.mixer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.contraptions.processing.ProcessingIngredient;
import com.simibubi.create.content.contraptions.processing.ProcessingOutput;
import com.simibubi.create.content.contraptions.processing.ProcessingRecipe;
import com.simibubi.create.content.contraptions.processing.BasinTileEntity.BasinInputInventory;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class MixingRecipe extends ProcessingRecipe<BasinInputInventory> {

	public MixingRecipe(ResourceLocation id, String group, List<ProcessingIngredient> ingredients,
			List<ProcessingOutput> results, int processingDuration) {
		super(AllRecipeTypes.MIXING, id, group, ingredients, results, processingDuration);
	}

	@Override
	protected int getMaxInputCount() {
		return 9;
	}
	
	@Override
	protected int getMaxOutputCount() {
		return 1;
	}
	
	@Override
	protected boolean canHaveCatalysts() {
		return true;
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

	public static MixingRecipe of(IRecipe<?> recipe) {
		return new MixingRecipe(recipe.getId(), recipe.getGroup(), ProcessingIngredient.list(recipe.getIngredients()),
				Arrays.asList(new ProcessingOutput(recipe.getRecipeOutput(), 1)), -1);
	}

}
