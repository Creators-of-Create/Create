package com.simibubi.create.content.contraptions.fluids.actors;

import java.util.List;
import java.util.Optional;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.foundation.fluid.FluidIngredient;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.wrappers.FluidBucketWrapper;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RecipeWrapper;

public class FillingBySpout {

	static RecipeWrapper wrapper = new RecipeWrapper(new ItemStackHandler(1));

	public static boolean canItemBeFilled(World world, ItemStack stack) {
		wrapper.setInventorySlotContents(0, stack);
		if (world.getRecipeManager()
			.getRecipe(AllRecipeTypes.FILLING.getType(), wrapper, world)
			.isPresent())
			return true;

		LazyOptional<IFluidHandlerItem> capability =
			stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY);
		IFluidHandlerItem tank = capability.orElse(null);
		if (tank == null)
			return false;
		for (int i = 0; i < tank.getTanks(); i++) {
			if (tank.getFluidInTank(i)
				.getAmount() < tank.getTankCapacity(i))
				return true;
		}
		return false;
	}

	public static int getRequiredAmountForItem(World world, ItemStack stack, FluidStack availableFluid) {
		wrapper.setInventorySlotContents(0, stack);
		Optional<IRecipe<RecipeWrapper>> recipe = world.getRecipeManager()
			.getRecipe(AllRecipeTypes.FILLING.getType(), wrapper, world);
		if (recipe.isPresent()) {
			FillingRecipe fillingRecipe = (FillingRecipe) recipe.get();
			FluidIngredient requiredFluid = fillingRecipe.getRequiredFluid();
			if (requiredFluid.test(availableFluid))
				return requiredFluid.getRequiredAmount();
		}

		LazyOptional<IFluidHandlerItem> capability =
			stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY);
		IFluidHandlerItem tank = capability.orElse(null);
		if (tank == null)
			return -1;
		if (tank instanceof FluidBucketWrapper)
			return 1000;

		int filled = tank.fill(availableFluid, FluidAction.SIMULATE);
		return filled == 0 ? -1 : filled;
	}

	public static ItemStack fillItem(World world, int requiredAmount, ItemStack stack, FluidStack availableFluid) {
		FluidStack toFill = availableFluid.copy();
		toFill.setAmount(requiredAmount);
		availableFluid.shrink(requiredAmount);

		wrapper.setInventorySlotContents(0, stack);
		Optional<IRecipe<RecipeWrapper>> recipe = world.getRecipeManager()
			.getRecipe(AllRecipeTypes.FILLING.getType(), wrapper, world);
		if (recipe.isPresent()) {
			FillingRecipe fillingRecipe = (FillingRecipe) recipe.get();
			FluidIngredient requiredFluid = fillingRecipe.getRequiredFluid();
			if (requiredFluid.test(toFill)) {
				List<ItemStack> results = fillingRecipe.rollResults();
				stack.shrink(1);
				return results.isEmpty() ? ItemStack.EMPTY : results.get(0);
			}
		}

		ItemStack split = stack.copy();
		split.setCount(1);
		LazyOptional<IFluidHandlerItem> capability =
			split.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY);
		IFluidHandlerItem tank = capability.orElse(null);
		if (tank == null)
			return ItemStack.EMPTY;
		tank.fill(toFill, FluidAction.EXECUTE);
		ItemStack container = tank.getContainer()
			.copy();
		stack.shrink(1);
		return container;
	}

}
