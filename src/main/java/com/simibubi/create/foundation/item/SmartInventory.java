package com.simibubi.create.foundation.item;

import java.util.function.Consumer;

import javax.annotation.Nonnull;

import com.simibubi.create.foundation.tileEntity.SyncedTileEntity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RecipeWrapper;

public class SmartInventory extends RecipeWrapper
	implements IItemHandlerModifiableIntermediate, INBTSerializable<CompoundTag> {

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
		wrapped = (SyncedStackHandler) inv;
	}
	
	public SmartInventory withMaxStackSize(int maxStackSize) {
		stackSize = maxStackSize;
		wrapped.stackSize = maxStackSize;
		return this;
	}

	public SmartInventory whenContentsChanged(Consumer<Integer> updateCallback) {
		((SyncedStackHandler) inv).whenContentsChange(updateCallback);
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

	@Override
	public int getSlots() {
		return inv.getSlots();
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
		if (!insertionAllowed)
			return stack;
		return inv.insertItem(slot, stack, simulate);
	}

	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		if (!extractionAllowed)
			return ItemStack.EMPTY;
		if (stackNonStackables) {
			ItemStack extractItem = inv.extractItem(slot, amount, true);
			if (!extractItem.isEmpty() && extractItem.getMaxStackSize() < extractItem.getCount())
				amount = extractItem.getMaxStackSize();
		}
		return inv.extractItem(slot, amount, simulate);
	}

	@Override
	public int getSlotLimit(int slot) {
		return Math.min(inv.getSlotLimit(slot), stackSize);
	}

	@Override
	public boolean isItemValid(int slot, ItemStack stack) {
		return inv.isItemValid(slot, stack);
	}

	@Override
	public void setStackInSlot(int slot, ItemStack stack) {
		inv.setStackInSlot(slot, stack);
	}

	@Override
	public ItemStack getItem(int slot) {
		return super.getItem(slot);
	}

	public int getStackLimit(int slot, @Nonnull ItemStack stack) {
		return Math.min(getSlotLimit(slot), stack.getMaxStackSize());
	}

	@Override
	public CompoundTag serializeNBT() {
		return getInv().serializeNBT();
	}

	@Override
	public void deserializeNBT(CompoundTag nbt) {
		getInv().deserializeNBT(nbt);
	}

	private SyncedStackHandler getInv() {
		return (SyncedStackHandler) inv;
	}

	private static class SyncedStackHandler extends ItemStackHandler {

		private SyncedTileEntity te;
		private boolean stackNonStackables;
		private int stackSize;
		private Consumer<Integer> updateCallback;

		public SyncedStackHandler(int slots, SyncedTileEntity te, boolean stackNonStackables, int stackSize) {
			super(slots);
			this.te = te;
			this.stackNonStackables = stackNonStackables;
			this.stackSize = stackSize;
		}

		@Override
		protected void onContentsChanged(int slot) {
			super.onContentsChanged(slot);
			if (updateCallback != null)
				updateCallback.accept(slot);
			te.notifyUpdate();
		}

		@Override
		public int getSlotLimit(int slot) {
			return Math.min(stackNonStackables ? 64 : super.getSlotLimit(slot), stackSize);
		}

		public void whenContentsChange(Consumer<Integer> updateCallback) {
			this.updateCallback = updateCallback;
		}

	}

	@Override
	public ItemStack getStackInSlotIntermediate(int slot) {
		return getItem(slot);
	}

}
