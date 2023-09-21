package com.simibubi.create.foundation.blockEntity.behaviour.inventory;

import java.util.concurrent.atomic.AtomicInteger;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;

public class VersionedInventoryWrapper implements IItemHandlerModifiable {

	public static final AtomicInteger idGenerator = new AtomicInteger();
	
	private IItemHandlerModifiable inventory;
	private int version;
	private int id;

	public VersionedInventoryWrapper(IItemHandlerModifiable inventory) {
		this.id = idGenerator.getAndIncrement();
		this.inventory = inventory;
		this.version = 0;
	}

	public void incrementVersion() {
		version++;
	}

	public int getVersion() {
		return version;
	}
	
	public int getId() {
		return id;
	}

	//

	@Override
	public int getSlots() {
		return inventory.getSlots();
	}

	@Override
	public int getSlotLimit(int slot) {
		return inventory.getSlotLimit(slot);
	}

	@Override
	public boolean isItemValid(int slot, ItemStack stack) {
		return inventory.isItemValid(slot, stack);
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		return inventory.getStackInSlot(slot);
	}

	//

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
		int count = stack.getCount();
		ItemStack result = inventory.insertItem(slot, stack, simulate);
		if (!simulate && count != result.getCount())
			incrementVersion();
		return result;
	}

	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		ItemStack result = inventory.extractItem(slot, amount, simulate);
		if (!simulate && !result.isEmpty())
			incrementVersion();
		return result;
	}

	@Override
	public void setStackInSlot(int slot, ItemStack stack) {
		ItemStack previousItem = inventory.getStackInSlot(slot);
		inventory.setStackInSlot(slot, stack);

		if (stack.isEmpty() == previousItem.isEmpty()) {
			if (stack.isEmpty())
				return;
			if (ItemHandlerHelper.canItemStacksStack(stack, previousItem)
				&& stack.getCount() == previousItem.getCount())
				return;
		}

		incrementVersion();
	}

}
