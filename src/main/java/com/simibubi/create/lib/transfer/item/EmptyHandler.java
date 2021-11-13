package com.simibubi.create.lib.transfer.item;

import net.minecraft.world.item.ItemStack;

public class EmptyHandler implements IItemHandlerModifiable {
	public static final EmptyHandler INSTANCE = new EmptyHandler();

	private EmptyHandler() {}

	@Override
	public int getSlots() {
		return 0;
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		return ItemStack.EMPTY;
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean sim) {
		return stack;
	}

	@Override
	public ItemStack extractItem(int slot, int amount, boolean sim) {
		return ItemStack.EMPTY;
	}

	@Override
	public int getSlotLimit(int slot) {
		return 0;
	}

	@Override
	public boolean isItemValid(int slot, ItemStack stack) {
		return false;
	}

	@Override
	public void setStackInSlot(int slot, ItemStack stack) {}
}
