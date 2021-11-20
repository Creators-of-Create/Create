package com.simibubi.create.lib.transfer.item;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.world.item.ItemStack;

// this class is awful, but we don't have many options

/**
 * Wraps a Storage in an IItemHandler, for use in Create
 */
@SuppressWarnings({"UnstableApiUsage", "deprecation"})
public class ItemStorageHandler implements IItemHandlerModifiable {
	protected final Storage<ItemVariant> storage;

	public ItemStorageHandler(Storage<ItemVariant> storage) {
		this.storage = storage;
	}

	@Override
	public int getSlots() {
		int slots = 0;
		try (Transaction t = Transaction.openOuter()) {
			for (StorageView<ItemVariant> view : storage.iterable(t)) {
				slots++;
			}
			t.abort();
		}
		return slots;
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		try (Transaction t = Transaction.openOuter()) {
			int index = 0;
			for (StorageView<ItemVariant> view : storage.iterable(t)) {
				if (index == slot) {
					return view.getResource().toStack((int) view.getAmount());
				}
				index++;
			}
			t.abort();
		}
		return ItemStack.EMPTY;
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean sim) {
		ItemStack finalVal = ItemStack.EMPTY;
		try (Transaction t = Transaction.openOuter()) {
			long remainder = storage.insert(ItemVariant.of(stack), stack.getCount(), t);
			if (remainder != 0) {
				finalVal = new ItemStack(stack.getItem(), (int) remainder);
			}

			if (!sim) {
				t.commit();
			}
		}
		return finalVal;
	}

	@Override
	public ItemStack extractItem(int slot, int amount, boolean sim) {
		ItemStack finalVal = ItemStack.EMPTY;
		try (Transaction t = Transaction.openOuter()) {
			int index = 0;
			for (StorageView<ItemVariant> view : storage.iterable(t)) {
				if (index == slot) {
					ItemVariant variant = view.getResource();
					long extracted = view.isResourceBlank() ? 0 : storage.extract(variant, amount, t);
					if (extracted != 0) {
						finalVal = variant.toStack((int) extracted);
					}
					break;
				}
				index++;
			}
			if (!sim) {
				t.commit();
			}
		}
		return finalVal;
	}

	@Override
	public int getSlotLimit(int slot) {
		return 64;
	}

	@Override
	public boolean isItemValid(int slot, ItemStack stack) {
		return true;
	}

	@Override
	public void setStackInSlot(int slot, ItemStack stack) {
		// jank
		extractItem(slot, getSlotLimit(slot), false);
		insertItem(slot, stack, false);
	}
}
