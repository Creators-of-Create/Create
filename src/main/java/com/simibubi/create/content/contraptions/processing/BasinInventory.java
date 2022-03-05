package com.simibubi.create.content.contraptions.processing;

import com.simibubi.create.foundation.item.SmartInventory;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemHandlerHelper;

import net.minecraft.world.item.ItemStack;

public class BasinInventory extends SmartInventory {

	private BasinTileEntity te;

	public BasinInventory(int slots, BasinTileEntity te) {
		super(slots, te, 16, true);
		this.te = te;
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
		// Only insert if no other slot already has a stack of this item
		for (int i = 0; i < getSlots(); i++)
			if (i != slot && ItemHandlerHelper.canItemStacksStack(stack, inv.getStackInSlot(i)))
				return stack;
		return super.insertItem(slot, stack, simulate);
	}

	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		ItemStack extractItem = super.extractItem(slot, amount, simulate);
		if (!simulate && !extractItem.isEmpty())
			te.notifyChangeOfContents();
		return extractItem;
	}

}
