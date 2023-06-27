package com.simibubi.create.content.fluids.transfer;

import java.util.List;
import java.util.Optional;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.fluids.potion.PotionFluidHandler;
import com.simibubi.create.foundation.utility.Pair;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RecipeWrapper;

public class GenericItemEmptying {

	private static final RecipeWrapper WRAPPER = new RecipeWrapper(new ItemStackHandler(1));

	public static boolean canItemBeEmptied(Level world, ItemStack stack) {
		if (stack.getItem() instanceof PotionItem)
			return true;
		
		WRAPPER.setItem(0, stack);
		if (AllRecipeTypes.EMPTYING.find(WRAPPER, world)
			.isPresent())
			return true;

		LazyOptional<IFluidHandlerItem> capability =
			stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY);
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
		FluidStack resultingFluid = FluidStack.EMPTY;
		ItemStack resultingItem = ItemStack.EMPTY;

		if (stack.getItem() instanceof PotionItem)
			return PotionFluidHandler.emptyPotion(stack, simulate);
		
		WRAPPER.setItem(0, stack);
		Optional<Recipe<RecipeWrapper>> recipe = AllRecipeTypes.EMPTYING.find(WRAPPER, world);
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
			split.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY);
		IFluidHandlerItem tank = capability.orElse(null);
		if (tank == null)
			return Pair.of(resultingFluid, resultingItem);
		resultingFluid = tank.drain(1000, simulate ? FluidAction.SIMULATE : FluidAction.EXECUTE);
		resultingItem = tank.getContainer()
			.copy();
		if (!simulate)
			stack.shrink(1);

		return Pair.of(resultingFluid, resultingItem);
	}

}
