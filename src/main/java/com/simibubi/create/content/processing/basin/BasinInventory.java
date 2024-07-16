package com.simibubi.create.content.processing.basin;

import com.simibubi.create.foundation.item.SmartInventory;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;

public class BasinInventory extends SmartInventory {

	private BasinBlockEntity blockEntity;

	public BasinInventory(int slots, BasinBlockEntity be) {
		super(slots, be, 16, true);
		this.blockEntity = be;
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
		int firstFreeSlot = -1;

		for (int i = 0; i < getSlots(); i++) {
			// Only insert if no other slot already has a stack of this item
			if (i != slot && ItemHandlerHelper.canItemStacksStack(stack, inv.getStackInSlot(i)))
				return stack;
			if (inv.getStackInSlot(i)
				.isEmpty() && firstFreeSlot == -1)
				firstFreeSlot = i;
		}

		// Only insert if this is the first empty slot, prevents overfilling in the
		// simulation pass
		if (inv.getStackInSlot(slot)
			.isEmpty() && firstFreeSlot != slot)
			return stack;

		return super.insertItem(slot, stack, simulate);
	}

	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		ItemStack extractItem = super.extractItem(slot, amount, simulate);
		if (!simulate && !extractItem.isEmpty())
			blockEntity.notifyChangeOfContents();
		return extractItem;
	}

}
