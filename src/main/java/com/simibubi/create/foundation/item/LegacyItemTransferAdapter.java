package com.simibubi.create.foundation.item;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class LegacyItemTransferAdapter extends SnapshotJournal<List<ItemStack>> implements ResourceHandler<ItemResource> {
	private final IItemHandler handler;

	public LegacyItemTransferAdapter(IItemHandler handler) {
		this.handler = handler;
	}

	@Override
	public int size() {
		return handler.getSlots();
	}

	@Override
	public ItemResource getResource(int index) {
		if (index < 0 || index >= handler.getSlots())
			return ItemResource.EMPTY;
		return ItemResource.of(handler.getStackInSlot(index));
	}

	@Override
	public long getAmountAsLong(int index) {
		if (index < 0 || index >= handler.getSlots())
			return 0;
		return handler.getStackInSlot(index)
			.getCount();
	}

	@Override
	public long getCapacityAsLong(int index, ItemResource resource) {
		if (index < 0 || index >= handler.getSlots())
			return 0;
		return handler.getSlotLimit(index);
	}

	@Override
	public boolean isValid(int index, ItemResource resource) {
		if (index < 0 || index >= handler.getSlots() || resource.isEmpty())
			return false;
		return handler.isItemValid(index, resource.toStack(1));
	}

	@Override
	public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
		if (!isValid(index, resource) || amount <= 0)
			return 0;
		ItemStack stack = resource.toStack(amount);
		int inserted = amount - handler.insertItem(index, stack, true).getCount();
		if (inserted <= 0)
			return 0;
		updateSnapshots(transaction);
		return inserted - handler.insertItem(index, stack.copyWithCount(inserted), false).getCount();
	}

	@Override
	public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
		if (index < 0 || index >= handler.getSlots() || resource.isEmpty() || amount <= 0)
			return 0;
		ItemStack inSlot = handler.getStackInSlot(index);
		if (inSlot.isEmpty() || !resource.matches(inSlot))
			return 0;
		ItemStack extracted = handler.extractItem(index, amount, true);
		if (extracted.isEmpty())
			return 0;
		updateSnapshots(transaction);
		return handler.extractItem(index, extracted.getCount(), false)
			.getCount();
	}

	@Override
	protected List<ItemStack> createSnapshot() {
		List<ItemStack> snapshot = new ArrayList<>(handler.getSlots());
		for (int i = 0; i < handler.getSlots(); i++)
			snapshot.add(handler.getStackInSlot(i).copy());
		return snapshot;
	}

	@Override
	protected void revertToSnapshot(List<ItemStack> snapshot) {
		if (!(handler instanceof IItemHandlerModifiable modifiable))
			return;
		for (int i = 0; i < snapshot.size() && i < modifiable.getSlots(); i++)
			modifiable.setStackInSlot(i, snapshot.get(i).copy());
	}
}
