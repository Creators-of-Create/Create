package com.simibubi.create.content.logistics.block.depot;

import com.simibubi.create.content.contraptions.relays.belt.transport.TransportedItemStack;

import io.github.fabricators_of_create.porting_lib.transfer.ViewOnlyWrappedStorageView;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemHandlerHelper;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemStackHandler;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.world.item.ItemStack;

import java.util.Iterator;

public class DepotItemHandler implements Storage<ItemVariant> {

	private static final int MAIN_SLOT = 0;
	private DepotBehaviour te;

	public DepotItemHandler(DepotBehaviour te) {
		this.te = te;
	}

	@Override
	public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		if (!te.getHeldItemStack()
				.isEmpty() && !te.canMergeItems())
			return 0;
		if (!te.isOutputEmpty() && !te.canMergeItems())
			return 0;
		te.snapshotParticipant.updateSnapshots(transaction);
		int maxInsert = (int) Math.min(maxAmount, resource.getItem().getMaxStackSize());
		ItemStack remainder = te.insert(new TransportedItemStack(resource.toStack(maxInsert)), transaction);
		return maxInsert - remainder.getCount();
	}

	@Override
	public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		long extracted = te.processingOutputBuffer.extract(resource, maxAmount, transaction);
		if (extracted == 0) {
			TransportedItemStack held = te.heldItem;
			if (held == null)
				return 0;
			ItemStack stack = held.stack.copy();
			extracted = Math.min(maxAmount, stack.getCount());
			stack.shrink((int) extracted);
			te.snapshotParticipant.updateSnapshots(transaction);
			te.heldItem.stack = stack;
			if (stack.isEmpty())
				te.heldItem = null;
		}
		return extracted;
	}

	@Override
	public Iterator<StorageView<ItemVariant>> iterator(TransactionContext transaction) {
		return new DepotItemHandlerIterator(transaction);
	}

	public class DepotItemHandlerIterator implements Iterator<StorageView<ItemVariant>> {
		private final Iterator<StorageView<ItemVariant>> outputItr;
		private boolean main = true;

		public DepotItemHandlerIterator(TransactionContext ctx) {
			outputItr = te.processingOutputBuffer.iterator(ctx);
			ctx.addCloseCallback((t, r) -> main = false); // outputItr invalidates itself so this is fine
		}

		@Override
		public boolean hasNext() {
			return main || outputItr.hasNext();
		}

		@Override
		public StorageView<ItemVariant> next() {
			if (main) {
				main = false;
				return new MainSlotStorageView();
			} else {
				return outputItr.next();
			}
		}
	}

	public class MainSlotStorageView implements StorageView<ItemVariant> {

		private ItemVariant var;
		private int count;
		private int max;

		private final ItemStack stack;

		public MainSlotStorageView() {
			TransportedItemStack stack = te.heldItem;
			this.stack = stack == null ? ItemStack.EMPTY : stack.stack;
			update();
		}

		private void update() {
			this.var = ItemVariant.of(stack);
			this.count = stack.getCount();
			this.max = stack.getMaxStackSize();
		}

		@Override
		public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
			if (resource.matches(stack)) {
				int toExtract = (int) Math.min(count, maxAmount);
				if (toExtract != 0) {
					te.snapshotParticipant.updateSnapshots(transaction);
					stack.shrink(toExtract);
					update();
					return toExtract;
				}
			}
			return 0;
		}

		@Override
		public boolean isResourceBlank() {
			return var.isBlank();
		}

		@Override
		public ItemVariant getResource() {
			return var;
		}

		@Override
		public long getAmount() {
			return count;
		}

		@Override
		public long getCapacity() {
			return max;
		}
	}
}
