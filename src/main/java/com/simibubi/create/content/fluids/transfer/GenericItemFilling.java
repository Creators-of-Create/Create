package com.simibubi.create.content.fluids.transfer;

import com.simibubi.create.AllFluids;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.fluids.potion.PotionFluidHandler;
import com.simibubi.create.foundation.fluid.FluidHelper;

import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MilkBucketItem;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.fluids.capability.wrappers.FluidBucketWrapper;

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
			if (item.getClass() != BucketItem.class && !(item instanceof MilkBucketItem)) {
				return false;
			}
		}
		return true;
	}

	public static boolean canItemBeFilled(Level world, ItemStack stack) {
		if (stack.getItem() == Items.GLASS_BOTTLE)
			return true;
		if (stack.getItem() == Items.MILK_BUCKET)
			return false;

		IFluidHandlerItem capability = stack.getCapability(Capabilities.FluidHandler.ITEM);
		if (capability == null)
			return false;
		if (!isFluidHandlerValid(stack, capability))
			return false;
		for (int i = 0; i < capability.getTanks(); i++) {
			if (capability.getFluidInTank(i)
				.getAmount() < capability.getTankCapacity(i))
				return true;
		}
		return false;
	}

	public static int getRequiredAmountForItem(Level world, ItemStack stack, FluidStack availableFluid) {
		if (stack.getItem() == Items.GLASS_BOTTLE && canFillGlassBottleInternally(availableFluid))
			return PotionFluidHandler.getRequiredAmountForFilledBottle(stack, availableFluid);
		if (stack.getItem() == Items.BUCKET && canFillBucketInternally(availableFluid))
			return 1000;

		IFluidHandlerItem capability = stack.getCapability(Capabilities.FluidHandler.ITEM);
		if (capability == null)
			return -1;
		if (capability instanceof FluidBucketWrapper) {
			Item filledBucket = availableFluid.getFluid()
				.getBucket();
			if (filledBucket == null || filledBucket == Items.AIR)
				return -1;
			if (!((FluidBucketWrapper) capability).getFluid()
				.isEmpty())
				return -1;
			return 1000;
		}

		int filled = capability.fill(availableFluid, FluidAction.SIMULATE);
		return filled == 0 ? -1 : filled;
	}

	private static boolean canFillGlassBottleInternally(FluidStack availableFluid) {
		Fluid fluid = availableFluid.getFluid();
		if (fluid.isSame(Fluids.WATER))
			return true;
		if (fluid.isSame(AllFluids.POTION.get()))
			return true;
		if (fluid.isSame(AllFluids.TEA.get()))
			return true;
		return false;
	}

	private static boolean canFillBucketInternally(FluidStack availableFluid) {
		return false;
	}

	public static ItemStack fillItem(Level world, int requiredAmount, ItemStack stack, FluidStack availableFluid) {
		FluidStack toFill = availableFluid.copy();
		toFill.setAmount(requiredAmount);
		availableFluid.shrink(requiredAmount);

		if (stack.getItem() == Items.GLASS_BOTTLE && canFillGlassBottleInternally(toFill)) {
			ItemStack fillBottle;
			Fluid fluid = toFill.getFluid();
			if (FluidHelper.isWater(fluid))
				fillBottle = PotionContents.createItemStack(Items.POTION, Potions.WATER);
			else if (fluid.isSame(AllFluids.TEA.get()))
				fillBottle = AllItems.BUILDERS_TEA.asStack();
			else
				fillBottle = PotionFluidHandler.fillBottle(stack, toFill);
			stack.shrink(1);
			return fillBottle;
		}

		ItemStack split = stack.copy();
		split.setCount(1);
		IFluidHandlerItem capability = split.getCapability(Capabilities.FluidHandler.ITEM);
		if (capability == null)
			return ItemStack.EMPTY;
		capability.fill(toFill, FluidAction.EXECUTE);
		ItemStack container = capability.getContainer()
			.copy();
		stack.shrink(1);
		return container;
	}

}
