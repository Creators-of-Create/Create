package com.simibubi.create.content.contraptions.processing;

import java.util.List;
import java.util.Optional;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.contraptions.fluids.potion.PotionFluidHandler;
import com.simibubi.create.foundation.utility.Pair;
import com.simibubi.create.lib.transfer.TransferUtil;
import com.simibubi.create.lib.transfer.fluid.FluidStack;
import com.simibubi.create.lib.transfer.fluid.IFluidHandlerItem;
import com.simibubi.create.lib.transfer.item.ItemStackHandler;
import com.simibubi.create.lib.transfer.item.RecipeWrapper;
import com.simibubi.create.lib.util.LazyOptional;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;

public class EmptyingByBasin {

	static RecipeWrapper wrapper = new RecipeWrapper(new ItemStackHandler(1));

	public static boolean canItemBeEmptied(Level world, ItemStack stack) {
		if (stack.getItem() instanceof PotionItem)
			return true;

		wrapper.setItem(0, stack);
		if (AllRecipeTypes.EMPTYING.find(wrapper, world)
			.isPresent())
			return true;

		LazyOptional<IFluidHandlerItem> capability =
				TransferUtil.getFluidHandlerItem(stack);
		IFluidHandlerItem tank = capability.orElse(null);
		if (tank == null)
			return false;
		for (int i = 0; i < tank.getTanks(); i++) {
			if (tank.getFluidInTank(i)
				.getAmount() > 0)
				return true;
		}
		return false;
	}

	public static Pair<FluidStack, ItemStack> emptyItem(Level world, ItemStack stack, boolean simulate) {
		FluidStack resultingFluid = FluidStack.empty();
		ItemStack resultingItem = ItemStack.EMPTY;

		if (stack.getItem() instanceof PotionItem)
			return PotionFluidHandler.emptyPotion(stack, simulate);

		wrapper.setItem(0, stack);
		Optional<Recipe<RecipeWrapper>> recipe = AllRecipeTypes.EMPTYING.find(wrapper, world);
		if (recipe.isPresent()) {
			EmptyingRecipe emptyingRecipe = (EmptyingRecipe) recipe.get();
			List<ItemStack> results = emptyingRecipe.rollResults();
			if (!simulate)
				stack.shrink(1);
			resultingItem = results.isEmpty() ? ItemStack.EMPTY : results.get(0);
			resultingFluid = emptyingRecipe.getResultingFluid();
			return Pair.of(resultingFluid, resultingItem);
		}

		ItemStack split = stack.copy();
		split.setCount(1);
		LazyOptional<IFluidHandlerItem> capability =
			TransferUtil.getFluidHandlerItem(split);
		IFluidHandlerItem tank = capability.orElse(null);
		if (tank == null)
			return Pair.of(resultingFluid, resultingItem);
		resultingFluid = tank.drain(FluidConstants.BUCKET, simulate);
		resultingItem = tank.getContainer()
			.copy();
		if (!simulate)
			stack.shrink(1);

		return Pair.of(resultingFluid, resultingItem);
	}

}
