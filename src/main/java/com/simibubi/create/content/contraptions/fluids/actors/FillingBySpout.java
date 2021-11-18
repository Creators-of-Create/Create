package com.simibubi.create.content.contraptions.fluids.actors;

import java.util.List;
import java.util.Optional;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.contraptions.itemAssembly.SequencedAssemblyRecipe;
import com.simibubi.create.foundation.fluid.FluidIngredient;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;

import com.simibubi.create.lib.transfer.fluid.FluidStack;

import com.simibubi.create.lib.transfer.item.ItemStackHandler;
import com.simibubi.create.lib.transfer.item.RecipeWrapper;

import com.simibubi.create.lib.transfer.fluid.FluidStack;
import com.simibubi.create.lib.transfer.item.ItemStackHandler;
import com.simibubi.create.lib.transfer.item.RecipeWrapper;

public class FillingBySpout {

	static RecipeWrapper wrapper = new RecipeWrapper(new ItemStackHandler(1));

	public static boolean canItemBeFilled(Level world, ItemStack stack) {
		wrapper.setItem(0, stack);

		Optional<FillingRecipe> assemblyRecipe =
			SequencedAssemblyRecipe.getRecipe(world, wrapper, AllRecipeTypes.FILLING.getType(), FillingRecipe.class);
		if (assemblyRecipe.isPresent())
			return true;

		if (AllRecipeTypes.FILLING.find(wrapper, world)
			.isPresent())
			return true;
		return GenericItemFilling.canItemBeFilled(world, stack);
	}

	public static long getRequiredAmountForItem(Level world, ItemStack stack, FluidStack availableFluid) {
		wrapper.setItem(0, stack);

		Optional<FillingRecipe> assemblyRecipe =
			SequencedAssemblyRecipe.getRecipe(world, wrapper, AllRecipeTypes.FILLING.getType(), FillingRecipe.class);
		if (assemblyRecipe.isPresent()) {
			FluidIngredient requiredFluid = assemblyRecipe.get()
				.getRequiredFluid();
			if (requiredFluid.test(availableFluid))
				return requiredFluid.getRequiredAmount();
		}

		for (Recipe<RecipeWrapper> recipe : world.getRecipeManager()
			.getRecipesFor(AllRecipeTypes.FILLING.getType(), wrapper, world)) {
			FillingRecipe fillingRecipe = (FillingRecipe) recipe;
			FluidIngredient requiredFluid = fillingRecipe.getRequiredFluid();
			if (requiredFluid.test(availableFluid))
				return requiredFluid.getRequiredAmount();
		}
		return GenericItemFilling.getRequiredAmountForItem(world, stack, availableFluid);
	}

	public static ItemStack fillItem(Level world, long requiredAmount, ItemStack stack, FluidStack availableFluid) {
		FluidStack toFill = availableFluid.copy();
		toFill.setAmount(requiredAmount);

		wrapper.setItem(0, stack);

		FillingRecipe fillingRecipe =
			SequencedAssemblyRecipe.getRecipe(world, wrapper, AllRecipeTypes.FILLING.getType(), FillingRecipe.class)
				.filter(fr -> fr.getRequiredFluid()
					.test(toFill))
				.orElseGet(() -> {
					for (Recipe<RecipeWrapper> recipe : world.getRecipeManager()
						.getRecipesFor(AllRecipeTypes.FILLING.getType(), wrapper, world)) {
						FillingRecipe fr = (FillingRecipe) recipe;
						FluidIngredient requiredFluid = fr.getRequiredFluid();
						if (requiredFluid.test(toFill))
							return fr;
					}
					return null;
				});

		if (fillingRecipe != null) {
			List<ItemStack> results = fillingRecipe.rollResults();
			availableFluid.shrink(requiredAmount);
			stack.shrink(1);
			return results.isEmpty() ? ItemStack.EMPTY : results.get(0);
		}

		return GenericItemFilling.fillItem(world, requiredAmount, stack, availableFluid);
	}

}
