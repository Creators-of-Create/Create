package com.simibubi.create.modules.logistics.block.inventories;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public class CreativeCrateInventory implements IItemHandler {

	private CreativeCrateTileEntity te;

	public CreativeCrateInventory(CreativeCrateTileEntity te) {
		this.te = te;
	}

	@Override
	public int getSlots() {
		return 2;
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		if (slot == 1)
			return ItemStack.EMPTY;
		ItemStack filter = te.filter.getFilter().copy();
		if (!filter.isEmpty())
			filter.setCount(filter.getMaxStackSize());
		return filter;
	}
	
	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
		return ItemStack.EMPTY;
	}

	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		ItemStack filter = te.filter.getFilter().copy();
		if (!filter.isEmpty())
			filter.setCount(amount);
		return filter;
	}

	@Override
	public int getSlotLimit(int slot) {
		return getStackInSlot(slot).getMaxStackSize();
	}

	@Override
	public boolean isItemValid(int slot, ItemStack stack) {
		return true;
	}

}
