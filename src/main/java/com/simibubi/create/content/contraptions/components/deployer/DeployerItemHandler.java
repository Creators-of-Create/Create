package com.simibubi.create.content.contraptions.components.deployer;

import com.simibubi.create.foundation.tileEntity.behaviour.filtering.FilteringBehaviour;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

import java.util.Iterator;
import java.util.ListIterator;

public class DeployerItemHandler extends SnapshotParticipant<ItemStack> implements Storage<ItemVariant> {

	private DeployerTileEntity te;
	private DeployerFakePlayer player;

	private ItemStack held = ItemStack.EMPTY; // intermediate storage for transactions

	public DeployerItemHandler(DeployerTileEntity te) {
		this.te = te;
		this.player = te.player;
	}

	public ItemStack getHeld() {
		if (player == null)
			return ItemStack.EMPTY;
		if (held != ItemStack.EMPTY)
			return held;
		return player.getMainHandItem();
	}

	public void set(ItemStack stack) {
		if (player == null)
			return;
		if (te.getLevel().isClientSide)
			return;
		this.held = stack;
	}

	@Override
	protected ItemStack createSnapshot() {
		return held.copy();
	}

	@Override
	protected void readSnapshot(ItemStack snapshot) {
		this.held = snapshot;
	}

	@Override
	protected void onFinalCommit() {
		super.onFinalCommit();
		player.setItemInHand(InteractionHand.MAIN_HAND, held);
		held = ItemStack.EMPTY;
		te.setChanged();
		te.sendData();
	}

	@Override
	public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		if (!isItemValid(resource.toStack()))
			return 0;

		ItemStack held = getHeld();
		if (held.isEmpty()) {
			updateSnapshots(transaction);
			set(resource.toStack((int) Math.min(resource.getItem().getMaxStackSize(), maxAmount)));
			return maxAmount;
		}

		if (!resource.matches(held))
			return 0;

		int space = held.getMaxStackSize() - held.getCount();
		if (space == 0)
			return 0;
		int toAdd = (int) Math.min(space, maxAmount);
		held = held.copy();
		held.setCount(held.getCount() + toAdd);
		updateSnapshots(transaction);
		set(held);

		return toAdd;
	}

	@Override
	public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		if (maxAmount == 0)
			return 0;

		for (ItemStack stack : te.overflowItems) {
			if (resource.matches(stack)) {
				long toExtract = Math.min(maxAmount, stack.getCount());
				te.snapshotParticipant.updateSnapshots(transaction);
				return stack.split((int) toExtract).getCount();
			}
		}

		ItemStack held = getHeld();
		if (held.isEmpty())
			return 0;
		if (!te.filtering.getFilter()
				.isEmpty() && te.filtering.test(held))
			return 0;
		long toExtract = Math.min(maxAmount, held.getCount());
		held.shrink((int) toExtract);
		if (held.isEmpty()) set(ItemStack.EMPTY);
		return toExtract;
	}

	@Override
	public Iterator<StorageView<ItemVariant>> iterator(TransactionContext transaction) {
		return new DeployerItemHandlerIterator(transaction);
	}

	public boolean isItemValid(ItemStack stack) {
		FilteringBehaviour filteringBehaviour = te.getBehaviour(FilteringBehaviour.TYPE);
		return filteringBehaviour == null || filteringBehaviour.test(stack);
	}

	public class DeployerItemHandlerIterator implements Iterator<StorageView<ItemVariant>> {
		private int index = 0;
		private boolean open = true;
		private boolean hand = false;

		public DeployerItemHandlerIterator(TransactionContext ctx) {
			ctx.addCloseCallback((t, r) -> open = false);
		}

		@Override
		public boolean hasNext() {
			return open && (index < te.overflowItems.size() || hand);
		}

		@Override
		public StorageView<ItemVariant> next() {
			if (index < te.overflowItems.size()) {
				return new DeployerSlotView(index++);
			} else {
				hand = false;
				return new DeployerHeldSlotView();
			}
		}
	}

	public class DeployerHeldSlotView extends SnapshotParticipant<ItemStack> implements StorageView<ItemVariant> {
		private ItemStack stack;
		private ItemVariant var;

		public DeployerHeldSlotView() {
			update();
		}

		private void update() {
			this.stack = getHeld();
			this.var = ItemVariant.of(stack);
		}

		@Override
		protected void onFinalCommit() {
			super.onFinalCommit();
			set(stack);
			update();
		}

		@Override
		protected ItemStack createSnapshot() {
			return stack.copy();
		}

		@Override
		protected void readSnapshot(ItemStack snapshot) {
			this.stack = snapshot;
			this.var = ItemVariant.of(stack);
		}

		@Override
		public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
			if (resource.matches(stack)) {
				if (stack.isEmpty())
					return 0;
				if (!te.filtering.getFilter().isEmpty() && te.filtering.test(stack))
					return 0;
				long toExtract = Math.min(maxAmount, stack.getCount());
				stack.shrink((int) toExtract);
				if (stack.isEmpty()) stack = ItemStack.EMPTY;
				return toExtract;
			}
			return 0;
		}

		@Override
		public boolean isResourceBlank() {
			return stack == null || stack.isEmpty();
		}

		@Override
		public ItemVariant getResource() {
			return var;
		}

		@Override
		public long getAmount() {
			return stack.getCount();
		}

		@Override
		public long getCapacity() {
			return stack.getMaxStackSize();
		}
	}

	public class DeployerSlotView extends SnapshotParticipant<ItemStack> implements StorageView<ItemVariant> {
		private final int index;
		private ItemStack stack;
		private ItemVariant var;

		public DeployerSlotView(int index) {
			this.index = index;
			update();
		}

		private void update() {
			this.stack = te.overflowItems.get(index).copy();
			this.var = ItemVariant.of(stack);
		}

		@Override
		protected void onFinalCommit() {
			super.onFinalCommit();
			te.overflowItems.set(index, stack);
			update();
		}

		@Override
		protected ItemStack createSnapshot() {
			return stack.copy();
		}

		@Override
		protected void readSnapshot(ItemStack snapshot) {
			this.stack = snapshot;
			this.var = ItemVariant.of(stack);
		}

		@Override
		public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
			if (resource.matches(stack)) {
				long toExtract = Math.min(stack.getCount(), maxAmount);
				updateSnapshots(transaction);
				ItemStack extracted = stack.split((int) toExtract);
				if (stack.isEmpty()) stack = ItemStack.EMPTY;
				return extracted.getCount();
			}
			return 0;
		}

		@Override
		public boolean isResourceBlank() {
			return stack.isEmpty();
		}

		@Override
		public ItemVariant getResource() {
			return var;
		}

		@Override
		public long getAmount() {
			return stack.getCount();
		}

		@Override
		public long getCapacity() {
			return stack.getItem().getMaxStackSize();
		}
	}
}
