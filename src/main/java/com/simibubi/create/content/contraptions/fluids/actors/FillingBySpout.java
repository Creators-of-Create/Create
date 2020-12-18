package com.simibubi.create.content.contraptions.fluids.actors;

import java.util.List;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.foundation.fluid.FluidIngredient;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RecipeWrapper;

public class FillingBySpout {

	static RecipeWrapper wrapper = new RecipeWrapper(new ItemStackHandler(1));

	public static boolean canItemBeFilled(World world, ItemStack stack) {
		wrapper.setInventorySlotContents(0, stack);
		if (AllRecipeTypes.FILLING.find(wrapper, world)
			.isPresent())
			return true;
		return GenericItemFilling.canItemBeFilled(world, stack);
	}

	public static int getRequiredAmountForItem(World world, ItemStack stack, FluidStack availableFluid) {
		wrapper.setInventorySlotContents(0, stack);
		for (IRecipe<RecipeWrapper> recipe : world.getRecipeManager()
			.getRecipes(AllRecipeTypes.FILLING.getType(), wrapper, world)) {
			FillingRecipe fillingRecipe = (FillingRecipe) recipe;
			FluidIngredient requiredFluid = fillingRecipe.getRequiredFluid();
			if (requiredFluid.test(availableFluid))
				return requiredFluid.getRequiredAmount();
		}
		return GenericItemFilling.getRequiredAmountForItem(world, stack, availableFluid);
	}

	public static ItemStack fillItem(World world, int requiredAmount, ItemStack stack, FluidStack availableFluid) {
		FluidStack toFill = availableFluid.copy();
		toFill.setAmount(requiredAmount);

		wrapper.setInventorySlotContents(0, stack);
		for (IRecipe<RecipeWrapper> recipe : world.getRecipeManager()
			.getRecipes(AllRecipeTypes.FILLING.getType(), wrapper, world)) {
			FillingRecipe fillingRecipe = (FillingRecipe) recipe;
			FluidIngredient requiredFluid = fillingRecipe.getRequiredFluid();
			if (requiredFluid.test(toFill)) {
				List<ItemStack> results = fillingRecipe.rollResults();
				availableFluid.shrink(requiredAmount);
				stack.shrink(1);
				return results.isEmpty() ? ItemStack.EMPTY : results.get(0);
			}
		}
		
		return GenericItemFilling.fillItem(world, requiredAmount, stack, availableFluid);
	}

}
