package com.simibubi.create.content.contraptions.processing;

import com.simibubi.create.foundation.item.SmartInventory;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;

public class BasinInventory extends SmartInventory {

	private BasinTileEntity te;

	public BasinInventory(int slots, BasinTileEntity te) {
		super(slots, te, 16, true);
		this.te = te;
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
		// Only insert if no other slot already has a stack of this item
		int firstEmpty = -1;
		for (int i = 0; i < getSlots(); i++) {
			if (i != slot && ItemHandlerHelper.canItemStacksStack(stack, inv.getStackInSlot(i)))
				return stack;

			if (inv.getStackInSlot(i).isEmpty() && firstEmpty == -1)
				firstEmpty = i;
		}

		if (inv.getStackInSlot(slot).isEmpty() && slot != firstEmpty)
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
