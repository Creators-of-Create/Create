package com.simibubi.create.foundation.item;

import java.util.Iterator;
import java.util.function.Consumer;

import javax.annotation.Nonnull;

import com.simibubi.create.foundation.tileEntity.SyncedTileEntity;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemStackHandler;
import io.github.fabricators_of_create.porting_lib.transfer.item.RecipeWrapper;
import io.github.fabricators_of_create.porting_lib.util.NBTSerializable;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class SmartInventory extends RecipeWrapper
	implements IItemHandlerModifiableIntermediate, NBTSerializable {

	protected boolean extractionAllowed;
	protected boolean insertionAllowed;
	protected boolean stackNonStackables;
	protected SyncedStackHandler wrapped;
	protected int stackSize;

	public SmartInventory(int slots, SyncedTileEntity te) {
		this(slots, te, 64, false);
	}

	public SmartInventory(int slots, SyncedTileEntity te, int stackSize, boolean stackNonStackables) {
		super(new SyncedStackHandler(slots, te, stackNonStackables, stackSize));
		this.stackNonStackables = stackNonStackables;
		insertionAllowed = true;
		extractionAllowed = true;
		this.stackSize = stackSize;
		wrapped = (SyncedStackHandler) this.handler;
	}

	public SmartInventory withMaxStackSize(int maxStackSize) {
		stackSize = maxStackSize;
		wrapped.stackSize = maxStackSize;
		return this;
	}

	public SmartInventory whenContentsChanged(Runnable updateCallback) {
		((SyncedStackHandler) this.handler).whenContentsChange(updateCallback);
		return this;
	}

	public SmartInventory allowInsertion() {
		insertionAllowed = true;
		return this;
	}

	public SmartInventory allowExtraction() {
		extractionAllowed = true;
		return this;
	}

	public SmartInventory forbidInsertion() {
		insertionAllowed = false;
		return this;
	}

	public SmartInventory forbidExtraction() {
		extractionAllowed = false;
		return this;
	}

//	@Override
//	public int getSlots() {
//		return inv.getSlots();
//	}


	@Override
	public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		if (!insertionAllowed)
			return 0;
		return handler.insert(resource, maxAmount, transaction);
	}

	@Override
	public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		if (!extractionAllowed)
			return 0;
		if (stackNonStackables) {
			try (Transaction t = transaction.openNested()) {
				long extracted = handler.extract(resource, maxAmount, t);
				t.abort();
				if (extracted != 0 && resource.getItem().getMaxStackSize() < extracted)
					maxAmount = resource.getItem().getMaxStackSize();
			}
		}
		return handler.extract(resource, maxAmount, transaction);
	}

//	@Override
//	public int getSlotLimit(int slot) {
//		return Math.min(inv.getSlotLimit(slot), stackSize);
//	}

//	@Override
//	public boolean isItemValid(int slot, ItemStack stack) {
//		return inv.isItemValid(slot, stack);
//	}

//	@Override
//	public void setStackInSlot(int slot, ItemStack stack) {
//		inv.setStackInSlot(slot, stack);
//	}

	@Override
	public ItemStack getItem(int slot) {
		return super.getItem(slot);
	}

//	public int getStackLimit(int slot, @Nonnull ItemStack stack) {
//		return Math.min(getSlotLimit(slot), stack.getMaxStackSize());
//	}

	@Override
	public CompoundTag serializeNBT() {
		return getInv().serializeNBT();
	}

	@Override
	public void deserializeNBT(CompoundTag nbt) {
		getInv().deserializeNBT(nbt);
	}

	private SyncedStackHandler getInv() {
		return (SyncedStackHandler) handler;
	}

	private static class SyncedStackHandler extends ItemStackHandler {

		private SyncedTileEntity te;
		private boolean stackNonStackables;
		private int stackSize;
		private Runnable updateCallback;

		public SyncedStackHandler(int slots, SyncedTileEntity te, boolean stackNonStackables, int stackSize) {
			super(slots);
			this.te = te;
			this.stackNonStackables = stackNonStackables;
			this.stackSize = stackSize;
		}

		@Override
		protected void onFinalCommit() {
			if (updateCallback != null)
				updateCallback.run();
			te.notifyUpdate();
		}

		@Override
		public int getSlotLimit(int slot) {
			return Math.min(stackNonStackables ? 64 : super.getSlotLimit(slot), stackSize);
		}

		public void whenContentsChange(Runnable updateCallback) {
			this.updateCallback = updateCallback;
		}

	}

	@Override
	public ItemStack getStackInSlotIntermediate(int slot) {
		return getItem(slot);
	}

	@Override
	public Iterator<StorageView<ItemVariant>> iterator(TransactionContext transaction) {
		return handler.iterator(transaction);
	}
}
