package com.simibubi.create.content.kinetics.belt.transport;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public class ItemHandlerBeltSegment implements IItemHandler {

	private final BeltInventory beltInventory;
	int offset;

	public ItemHandlerBeltSegment(BeltInventory beltInventory, int offset) {
		this.beltInventory = beltInventory;
		this.offset = offset;
	}

	@Override
	public int getSlots() {
		return 1;
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		TransportedItemStack stackAtOffset = this.beltInventory.getStackAtOffset(offset);
		if (stackAtOffset == null)
			return ItemStack.EMPTY;
		return stackAtOffset.stack;
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
		if (this.beltInventory.canInsertAt(offset)) {
			if (!simulate) {
				TransportedItemStack newStack = new TransportedItemStack(stack);
				newStack.insertedAt = offset;
				newStack.beltPosition = offset + .5f + (beltInventory.beltMovementPositive ? -1 : 1) / 16f;
				newStack.prevBeltPosition = newStack.beltPosition;
				this.beltInventory.addItem(newStack);
				this.beltInventory.belt.setChanged();
				this.beltInventory.belt.sendData();
			}
			return ItemStack.EMPTY;
		}
		return stack;
	}

	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		TransportedItemStack transported = this.beltInventory.getStackAtOffset(offset);
		if (transported == null)
			return ItemStack.EMPTY;

		amount = Math.min(amount, transported.stack.getCount());
		ItemStack extracted = simulate ? transported.stack.copy().split(amount) : transported.stack.split(amount);
		if (!simulate) {
			if (transported.stack.isEmpty())
				this.beltInventory.toRemove.add(transported);
			this.beltInventory.belt.setChanged();
			this.beltInventory.belt.sendData();
		}
		return extracted;
	}

	@Override
	public int getSlotLimit(int slot) {
		return Math.min(getStackInSlot(slot).getMaxStackSize(), 64);
	}

	@Override
	public boolean isItemValid(int slot, ItemStack stack) {
		return true;
	}

}