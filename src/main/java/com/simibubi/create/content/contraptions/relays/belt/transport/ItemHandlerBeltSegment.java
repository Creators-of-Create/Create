package com.simibubi.create.content.contraptions.relays.belt.transport;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleViewIterator;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

import java.util.Iterator;

public class ItemHandlerBeltSegment extends SnapshotParticipant<TransportedItemStack> implements Storage<ItemVariant>, StorageView<ItemVariant> {
	@Nullable // only not null when something has been inserted and transaction is still open
	private TransportedItemStack stack;
	private final BeltInventory beltInventory;
	int offset;

	public ItemHandlerBeltSegment(BeltInventory beltInventory, int offset) {
		this.beltInventory = beltInventory;
		this.offset = offset;
	}

	private boolean canInsert() {
		return this.beltInventory.canInsertAt(offset) && (stack == null || stack.stack.isEmpty());
	}

	private TransportedItemStack getStack() {
		if (stack != null && !stack.stack.isEmpty()) return stack;
		return this.beltInventory.getStackAtOffset(offset);
	}

	@Override
	protected void onFinalCommit() {
		if (stack != null) {
			this.beltInventory.addItem(stack);
			stack = null;
		}
		this.beltInventory.belt.setChanged();
		this.beltInventory.belt.sendData();
	}

	@Override
	public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		if (canInsert()) {
			updateSnapshots(transaction);
			int inserted = Math.min((int) maxAmount, resource.getItem().getMaxStackSize());
			stack = new TransportedItemStack(resource.toStack(inserted));
			stack.insertedAt = offset;
			stack.beltPosition = offset + .5f + (beltInventory.beltMovementPositive ? -1 : 1) / 16f;
			stack.prevBeltPosition = stack.beltPosition;
			return inserted;
		}
		return 0;
	}

	@Override
	public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		TransportedItemStack transported = getStack();
		if (transported == null)
			return 0;

		updateSnapshots(transaction);
		int amount = Math.min((int) maxAmount, transported.stack.getCount());
		ItemStack extracted = transported.stack.split(amount);
		return extracted.getCount();
	}

	@Override
	public boolean isResourceBlank() {
		return stack == null || stack.stack.isEmpty();
	}

	@Override
	public ItemVariant getResource() {
		return isResourceBlank() ? ItemVariant.blank() : ItemVariant.of(stack.stack);
	}

	@Override
	public long getAmount() {
		return isResourceBlank() ? 0 : stack.stack.getCount();
	}

	@Override
	public long getCapacity() {
		return isResourceBlank() ? 64 : stack.stack.getMaxStackSize();
	}

	@Override
	public Iterator<StorageView<ItemVariant>> iterator(TransactionContext transaction) {
		return SingleViewIterator.create(this, transaction);
	}

	@Override
	protected void readSnapshot(TransportedItemStack snapshot) {
		this.stack = snapshot == TransportedItemStack.EMPTY ? null : snapshot;
	}

	@Override
	protected TransportedItemStack createSnapshot() {
		return stack != null ? stack.copy() : TransportedItemStack.EMPTY;
	}
}
