package com.simibubi.create.content.logistics.depot;

import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public class DepotItemHandler implements IItemHandler {

	private static final int MAIN_SLOT = 0;
	private DepotBehaviour behaviour;

	public DepotItemHandler(DepotBehaviour behaviour) {
		this.behaviour = behaviour;
	}

	@Override
	public int getSlots() {
		return 9;
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		return slot == MAIN_SLOT ? behaviour.getHeldItemStack() : behaviour.processingOutputBuffer.getStackInSlot(slot - 1);
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
		if (slot != MAIN_SLOT)
			return stack;
		if (!behaviour.getHeldItemStack()
			.isEmpty() && !behaviour.canMergeItems())
			return stack;
		if (!behaviour.isOutputEmpty() && !behaviour.canMergeItems())
			return stack;

		ItemStack remainder = behaviour.insert(new TransportedItemStack(stack), simulate);
		if (!simulate && remainder != stack)
			behaviour.blockEntity.notifyUpdate();
		return remainder;
	}

	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		if (slot != MAIN_SLOT)
			return behaviour.processingOutputBuffer.extractItem(slot - 1, amount, simulate);

		TransportedItemStack held = behaviour.heldItem;
		if (held == null)
			return ItemStack.EMPTY;
		ItemStack stack = held.stack.copy();
		ItemStack extracted = stack.split(amount);
		if (!simulate) {
			behaviour.heldItem.stack = stack;
			if (stack.isEmpty())
				behaviour.heldItem = null;
			behaviour.blockEntity.notifyUpdate();
		}
		return extracted;
	}

	@Override
	public int getSlotLimit(int slot) {
		return slot == MAIN_SLOT ? behaviour.maxStackSize.get() : 64;
	}

	@Override
	public boolean isItemValid(int slot, ItemStack stack) {
		return slot == MAIN_SLOT && behaviour.isItemValid(stack);
	}

}
