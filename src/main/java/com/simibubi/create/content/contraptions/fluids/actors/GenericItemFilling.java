package com.simibubi.create.content.contraptions.fluids.actors;

import com.simibubi.create.AllFluids;
import com.simibubi.create.content.contraptions.fluids.potion.PotionFluidHandler;
import com.simibubi.create.foundation.fluid.FluidHelper;

import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.wrappers.FluidBucketWrapper;

public class GenericItemFilling {

	public static boolean canItemBeFilled(World world, ItemStack stack) {
		if (stack.getItem() == Items.GLASS_BOTTLE)
			return true;
		if (stack.getItem() == Items.MILK_BUCKET)
			return false;

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
		if (stack.getItem() == Items.GLASS_BOTTLE && canFillGlassBottleInternally(availableFluid))
			return PotionFluidHandler.getRequiredAmountForFilledBottle(stack, availableFluid);
		if (stack.getItem() == Items.BUCKET && canFillBucketInternally(availableFluid))
			return 1000;

		LazyOptional<IFluidHandlerItem> capability =
			stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY);
		IFluidHandlerItem tank = capability.orElse(null);
		if (tank == null)
			return -1;
		if (tank instanceof FluidBucketWrapper) {
			Item filledBucket = availableFluid.getFluid()
				.getFilledBucket();
			if (filledBucket == null || filledBucket == Items.AIR)
				return -1;
			if (!((FluidBucketWrapper) tank).getFluid()
				.isEmpty())
				return -1;
			return 1000;
		}

		int filled = tank.fill(availableFluid, FluidAction.SIMULATE);
		return filled == 0 ? -1 : filled;
	}

	private static boolean canFillGlassBottleInternally(FluidStack availableFluid) {
		return availableFluid.getFluid()
			.isEquivalentTo(Fluids.WATER)
			|| availableFluid.getFluid()
				.isEquivalentTo(AllFluids.POTION.get());
	}

	private static boolean canFillBucketInternally(FluidStack availableFluid) {
		return availableFluid.getFluid()
			.isEquivalentTo(AllFluids.MILK.get().getFlowingFluid());
	}

	public static ItemStack fillItem(World world, int requiredAmount, ItemStack stack, FluidStack availableFluid) {
		FluidStack toFill = availableFluid.copy();
		toFill.setAmount(requiredAmount);
		availableFluid.shrink(requiredAmount);

		if (stack.getItem() == Items.GLASS_BOTTLE && canFillGlassBottleInternally(toFill)) {
			ItemStack fillBottle = ItemStack.EMPTY;
			if (FluidHelper.isWater(toFill.getFluid()))
				fillBottle = PotionUtils.addPotionToItemStack(new ItemStack(Items.POTION), Potions.WATER);
			else
				fillBottle = PotionFluidHandler.fillBottle(stack, toFill);
			stack.shrink(1);
			return fillBottle;
		}
		
		if (stack.getItem() == Items.BUCKET && canFillBucketInternally(toFill)) {
			ItemStack filledBucket = new ItemStack(Items.MILK_BUCKET);
			stack.shrink(1);
			return filledBucket;
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
