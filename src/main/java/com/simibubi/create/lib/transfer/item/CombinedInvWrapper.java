package com.simibubi.create.lib.transfer.item;

import net.minecraft.world.item.ItemStack;

public class CombinedInvWrapper implements IItemHandlerModifiable {
	protected final IItemHandlerModifiable[] handlers;
	protected final int[] startIndices;
	protected final int totalSlots;

	public CombinedInvWrapper(IItemHandlerModifiable... handlers) {
		this.handlers = handlers;
		this.startIndices = new int[handlers.length];
		int indices = 0;
		for (int i = 0; i < handlers.length; i++) {
			indices += handlers[i].getSlots();
			startIndices[i] = indices;
		}
		totalSlots = indices;
	}

	protected int getIndexForSlot(int slot) {
		if (slot < 0) return -1;

		for (int i = 0; i < startIndices.length; i++) {
			if (slot - startIndices[i] < 0) {
				return i;
			}
		}
		return -1;
	}

	protected IItemHandlerModifiable getHandlerFromIndex(int index) {
		if (index < 0 || index >= handlers.length) {
			return EmptyHandler.INSTANCE;
		}
		return handlers[index];
	}

	protected int getSlotFromIndex(int slot, int index) {
		if (index <= 0 || index >= startIndices.length) {
			return slot;
		}
		return slot - startIndices[index - 1];
	}

	@Override
	public void setStackInSlot(int slot, ItemStack stack) {
		int index = getIndexForSlot(slot);
		IItemHandlerModifiable handler = getHandlerFromIndex(index);
		slot = getSlotFromIndex(slot, index);
		handler.setStackInSlot(slot, stack);
	}

	@Override
	public int getSlots() {
		return totalSlots;
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		int index = getIndexForSlot(slot);
		IItemHandlerModifiable handler = getHandlerFromIndex(index);
		slot = getSlotFromIndex(slot, index);
		return handler.getStackInSlot(slot);
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
		int index = getIndexForSlot(slot);
		IItemHandlerModifiable handler = getHandlerFromIndex(index);
		slot = getSlotFromIndex(slot, index);
		return handler.insertItem(slot, stack, simulate);
	}

	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		int index = getIndexForSlot(slot);
		IItemHandlerModifiable handler = getHandlerFromIndex(index);
		slot = getSlotFromIndex(slot, index);
		return handler.extractItem(slot, amount, simulate);
	}

	@Override
	public int getSlotLimit(int slot) {
		int index = getIndexForSlot(slot);
		IItemHandlerModifiable handler = getHandlerFromIndex(index);
		int localSlot = getSlotFromIndex(slot, index);
		return handler.getSlotLimit(localSlot);
	}

	@Override
	public boolean isItemValid(int slot, ItemStack stack) {
		int index = getIndexForSlot(slot);
		IItemHandlerModifiable handler = getHandlerFromIndex(index);
		int localSlot = getSlotFromIndex(slot, index);
		return handler.isItemValid(localSlot, stack);
	}
}
