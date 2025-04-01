package com.simibubi.create.content.fluids.transfer;

import java.util.List;

import com.simibubi.create.api.fluids.transfer.ItemFilling;

import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MilkBucketItem;
import net.minecraft.world.level.Level;

import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.wrappers.FluidBucketWrapper;

public class GenericItemFilling {

	/**
	 * Checks if an ItemStack's IFluidHandlerItem is valid. Ideally, this check would
	 * not be necessary. Unfortunately, some mods that copy the functionality of the
	 * MilkBucketItem copy the FluidBucketWrapper capability that is patched in by
	 * Forge without looking into what it actually does. In all cases this is
	 * incorrect because having a non-bucket item turn into a bucket item does not
	 * make sense.
	 *
	 * <p>This check is only necessary for filling since a FluidBucketWrapper will be
	 * empty if it is initialized with a non-bucket item.
	 *
	 * @param stack The ItemStack.
	 * @param fluidHandler The IFluidHandlerItem instance retrieved from the ItemStack.
	 * @return If the IFluidHandlerItem is valid for the passed ItemStack.
	 */
	public static boolean isFluidHandlerValid(ItemStack stack, IFluidHandlerItem fluidHandler) {
		// Not instanceof in case a correct subclass is made
		if (fluidHandler.getClass() == FluidBucketWrapper.class) {
			Item item = stack.getItem();
			// Forge does not patch the FluidBucketWrapper onto subclasses of BucketItem
            return item.getClass() == BucketItem.class || item instanceof MilkBucketItem;
		}
		return true;
	}

	public static boolean canItemBeFilled(Level world, ItemStack stack) {
		Item item = stack.getItem();
		if (item == Items.MILK_BUCKET)
			return false;
		List<ItemFilling> allFilling = ItemFilling.REGISTRY.get(item);
		if (!allFilling.isEmpty() && allFilling.stream().anyMatch(filling -> filling.canItemBeFilled(world, stack))) {
			return true;
		}

		LazyOptional<IFluidHandlerItem> capability =
			stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM);
		IFluidHandlerItem tank = capability.resolve().orElse(null);
		if (tank == null)
			return false;
		if (!isFluidHandlerValid(stack, tank))
			return false;
		for (int i = 0; i < tank.getTanks(); i++) {
			if (tank.getFluidInTank(i)
				.getAmount() < tank.getTankCapacity(i))
				return true;
		}
		return false;
	}

	public static int getRequiredAmountForItem(Level world, ItemStack stack, FluidStack availableFluid) {
		Item item = stack.getItem();
		List<ItemFilling> allFilling = ItemFilling.REGISTRY.get(item);
		if (!allFilling.isEmpty()) {
			for (ItemFilling filling : allFilling) {
				if (!filling.canItemBeFilled(world, stack))
					continue;
				int amount = filling.getRequiredAmountForItem(world, stack, availableFluid);
				if (amount >= 0)
					return amount;
			}
		}

		LazyOptional<IFluidHandlerItem> capability =
			stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM);
		IFluidHandlerItem tank = capability.resolve().orElse(null);
		if (tank == null)
			return -1;
		if (tank instanceof FluidBucketWrapper) {
			Item filledBucket = availableFluid.getFluid().getBucket();
			if (filledBucket == Items.AIR)
				return -1;
			if (!((FluidBucketWrapper) tank).getFluid()
				.isEmpty())
				return -1;
			return 1000;
		}

		int filled = tank.fill(availableFluid, FluidAction.SIMULATE);
		return filled == 0 ? -1 : filled;
	}

	public static ItemStack fillItem(Level world, int requiredAmount, ItemStack stack, FluidStack availableFluid) {
		FluidStack toFill = availableFluid.copy();
		toFill.setAmount(requiredAmount);
		availableFluid.shrink(requiredAmount);

		Item item = stack.getItem();
		List<ItemFilling> allFilling = ItemFilling.REGISTRY.get(item);
		if (!allFilling.isEmpty()) {
			for (ItemFilling filling : allFilling) {
				if (!filling.canItemBeFilled(world, stack))
					continue;
				return filling.fillItem(world, stack, toFill);
			}
		}

		ItemStack split = stack.copy();
		split.setCount(1);
		LazyOptional<IFluidHandlerItem> capability =
			split.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM);
		IFluidHandlerItem tank = capability.resolve().orElse(null);
		if (tank == null)
			return ItemStack.EMPTY;
		tank.fill(toFill, FluidAction.EXECUTE);
		ItemStack container = tank.getContainer()
			.copy();
		stack.shrink(1);
		return container;
	}

}
