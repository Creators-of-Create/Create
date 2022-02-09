package com.simibubi.create.content.contraptions.components.deployer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.simibubi.create.foundation.tileEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.lib.transfer.item.CustomStorageHandler;
import com.simibubi.create.lib.transfer.item.IItemHandlerModifiable;
import com.simibubi.create.lib.transfer.item.ItemHandlerHelper;

import com.simibubi.create.lib.transfer.item.StorageItemHandler;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

@SuppressWarnings("UnstableApiUsage")
public class DeployerItemHandler extends StorageItemHandler implements IItemHandlerModifiable, CustomStorageHandler {

	private DeployerTileEntity te;
	private DeployerFakePlayer player;

	public DeployerItemHandler(DeployerTileEntity te) {
		super(null);
		handler = this;
		this.te = te;
		this.player = te.player;
	}

	@Override
	public Storage<ItemVariant> getStorage() {
		return this;
	}

	@Override
	public Iterable<StorageView<ItemVariant>> iterable(TransactionContext transaction) {
																// need to store one handler for each stack, +1 for held stack
		List<StorageView<ItemVariant>> views = new ArrayList<>(te.overflowItems.size() + 1);
		views.add(new SlotStorageView(0, this)); // held stack
		optimizeOverflowItems();
		for (int i = 0; i < te.overflowItems.size(); i++) {
			views.add(new DeployerSlotHandler(i, te.overflowItems));
		}

		return views;
	}

	// over time the list could fill with random EMPTY values, this compresses and removes them
	private void optimizeOverflowItems() {
		List<ItemStack> newOverflowItems = new ArrayList<>();
		for (ItemStack stack : te.overflowItems) {
			if (!stack.isEmpty()) {
				newOverflowItems.add(stack);
			}
		}
		te.overflowItems = newOverflowItems;
	}

	@Override
	public int getSlots() {
		return 1;
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		return getHeld();
	}

	public ItemStack getHeld() {
		if (player == null)
			return ItemStack.EMPTY;
		return player.getMainHandItem();
	}

	public void set(ItemStack stack) {
		if (player == null)
			return;
		if (te.getLevel().isClientSide)
			return;
		player.setItemInHand(InteractionHand.MAIN_HAND, stack);
		te.setChanged();
		te.sendData();
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
		ItemStack held = getHeld();
		if (!isItemValid(slot, stack))
			return stack;
		if (held.isEmpty()) {
			if (!simulate)
				set(stack);
			return ItemStack.EMPTY;
		}
		if (!ItemHandlerHelper.canItemStacksStack(held, stack))
			return stack;

		int space = held.getMaxStackSize() - held.getCount();
		ItemStack remainder = stack.copy();
		ItemStack split = remainder.split(space);

		if (space == 0)
			return stack;
		if (!simulate) {
			held = held.copy();
			held.setCount(held.getCount() + split.getCount());
			set(held);
		}

		return remainder;
	}

	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		if (amount == 0)
			return ItemStack.EMPTY;

		ItemStack extractedFromOverflow = ItemStack.EMPTY;
		ItemStack returnToOverflow = ItemStack.EMPTY;

		for (Iterator<ItemStack> iterator = te.overflowItems.iterator(); iterator.hasNext();) {
			ItemStack existing = iterator.next();
			if (existing.isEmpty()) {
				iterator.remove();
				continue;
			}

			int toExtract = Math.min(amount, existing.getMaxStackSize());
			if (existing.getCount() <= toExtract) {
				if (!simulate)
					iterator.remove();
				extractedFromOverflow = existing;
				break;
			}
			if (!simulate) {
				iterator.remove();
				returnToOverflow = ItemHandlerHelper.copyStackWithSize(existing, existing.getCount() - toExtract);
			}
			extractedFromOverflow = ItemHandlerHelper.copyStackWithSize(existing, toExtract);
			break;
		}

		if (!returnToOverflow.isEmpty())
			te.overflowItems.add(returnToOverflow);
		if (!extractedFromOverflow.isEmpty())
			return extractedFromOverflow;

		ItemStack held = getHeld();
		if (amount == 0 || held.isEmpty())
			return ItemStack.EMPTY;
		if (!te.filtering.getFilter()
			.isEmpty() && te.filtering.test(held))
			return ItemStack.EMPTY;
		if (simulate)
			return held.copy()
				.split(amount);

		ItemStack toReturn = held.split(amount);
		te.setChanged();
		te.sendData();
		return toReturn;
	}

	@Override
	public int getSlotLimit(int slot) {
		return Math.min(getHeld().getMaxStackSize(), 64);
	}

	@Override
	public boolean isItemValid(int slot, ItemStack stack) {
		FilteringBehaviour filteringBehaviour = te.getBehaviour(FilteringBehaviour.TYPE);
		return filteringBehaviour == null || filteringBehaviour.test(stack);
	}

	@Override
	public void setStackInSlot(int slot, ItemStack stack) {
		set(stack);
	}

	private record DeployerSlotHandler(int index, List<ItemStack> items) implements StorageView<ItemVariant> {

		@Override
		public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
			if (valid()) {
				ItemStack toExtract = resource.toStack((int) maxAmount);
				ItemStack stored = items.get(index);
				if (stored.sameItem(toExtract)) {
					int actual = Math.min(stored.getCount(), (int) maxAmount);
					transaction.addCloseCallback((t, result) -> {
						if (result.wasCommitted() && valid()) {
							ItemStack newStored = stored.copy();
							newStored.setCount(stored.getCount() - actual);
							items.set(index, newStored.isEmpty() ? ItemStack.EMPTY : newStored);
						}
					});
					return actual;
				}
			}
			return 0;
		}

		@Override
		public boolean isResourceBlank() {
			if (valid()) {
				return items.get(index).isEmpty();
			}
			return true;
		}

		@Override
		public ItemVariant getResource() {
			if (valid()) {
				return ItemVariant.of(items.get(index));
			}
			return ItemVariant.blank();
		}

		@Override
		public long getAmount() {
			if (valid()) {
				return items.get(index).getCount();
			}
			return 0;
		}

		@Override
		public long getCapacity() {
			if (valid()) {
				return items.get(index).getMaxStackSize();
			}
			return 0;
		}

		private boolean valid() {
			return items.size() > index;
		}
	}
}
