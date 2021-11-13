package com.simibubi.create.lib.transfer.item;

import net.minecraft.world.item.ItemStack;

public interface IItemHandler {
	int getSlots();
	ItemStack getStackInSlot(int slot);
	ItemStack insertItem(int slot, ItemStack stack, boolean sim);
	ItemStack extractItem(int slot, int amount, boolean sim);
	int getSlotLimit(int slot);
	boolean isItemValid(int slot, ItemStack stack);
}
