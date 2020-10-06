package com.simibubi.create.content.contraptions.processing;

import com.simibubi.create.foundation.item.SmartInventory;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;

public class BasinInventory extends SmartInventory {

	public BasinInventory(int slots, BasinTileEntity te) {
		super(slots, te, 16, true);
	}
	
	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
		// Only insert if no other slot already has a stack of this item
		for (int i = 0; i < getSlots(); i++) 
			if (i != slot && ItemHandlerHelper.canItemStacksStack(stack, inv.getStackInSlot(i)))
				return stack;
		return super.insertItem(slot, stack, simulate);
	}

}
