package com.simibubi.create.lib.transfer.item;

import net.minecraft.world.item.ItemStack;

public interface IItemHandler {
	int getSlots();
	ItemStack getStackInSlot(int slot);
	ItemStack insertItem(int slot, ItemStack stack, boolean sim); // remainder
	ItemStack extractItem(int slot, int amount, boolean sim); // extracted
	int getSlotLimit(int slot);
	boolean isItemValid(int slot, ItemStack stack);
}
