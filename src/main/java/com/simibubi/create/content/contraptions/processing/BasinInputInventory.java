package com.simibubi.create.content.contraptions.processing;

import com.simibubi.create.foundation.item.SmartInventory;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;

public class BasinInputInventory extends SmartInventory {

	public BasinInputInventory(int slots, BasinTileEntity te) {
		super(slots, te);
	}
	
	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
		// Only insert if no other slot already has a 'full' stack of this item
		for (int i = 0; i < getSlots(); i++) {
			ItemStack stackInSlot = getStackInSlot(i);
			if (ItemHandlerHelper.canItemStacksStack(stack, stackInSlot)
				&& stackInSlot.getCount() == getStackLimit(i, stackInSlot))
				return stack;
		}

		return super.insertItem(slot, stack, simulate);
	}

}
