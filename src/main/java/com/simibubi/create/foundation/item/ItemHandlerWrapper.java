package com.simibubi.create.foundation.item;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;

public class ItemHandlerWrapper implements IItemHandlerModifiable {

	private IItemHandlerModifiable wrapped;

	public ItemHandlerWrapper(IItemHandlerModifiable wrapped) {
		this.wrapped = wrapped;
	}

	@Override
	public int getSlots() {
		return wrapped != null ? wrapped.getSlots() : 0;
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		return wrapped.getStackInSlot(slot);
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
		return wrapped.insertItem(slot, stack, simulate);
	}

	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		return wrapped.extractItem(slot, amount, simulate);
	}

	@Override
	public int getSlotLimit(int slot) {
		return wrapped.getSlotLimit(slot);
	}

	@Override
	public boolean isItemValid(int slot, ItemStack stack) {
		return wrapped.isItemValid(slot, stack);
	}

	@Override
	public void setStackInSlot(int slot, ItemStack stack) {
		wrapped.setStackInSlot(slot, stack);
	}

}
