package com.simibubi.create.foundation.item;

import java.util.function.Consumer;

import javax.annotation.Nonnull;

import com.simibubi.create.foundation.blockEntity.SyncedBlockEntity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RecipeWrapper;

public class SmartInventory extends RecipeWrapper
	implements IItemHandlerModifiable, INBTSerializable<CompoundTag> {

	protected boolean extractionAllowed;
	protected boolean insertionAllowed;
	protected boolean stackNonStackables;
	protected SyncedStackHandler wrapped;
	protected int stackSize;

	public SmartInventory(int slots, SyncedBlockEntity be) {
		this(slots, be, 64, false);
	}

	public SmartInventory(int slots, SyncedBlockEntity be, int stackSize, boolean stackNonStackables) {
		super(new SyncedStackHandler(slots, be, stackNonStackables, stackSize));
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
	public ItemStack getStackInSlot(int slot) {
		return inv.getStackInSlot(slot);
	}

	@Override
	public void setStackInSlot(int slot, ItemStack stack) {
		inv.setStackInSlot(slot, stack);
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

		private SyncedBlockEntity blockEntity;
		private boolean stackNonStackables;
		private int stackSize;
		private Consumer<Integer> updateCallback;

		public SyncedStackHandler(int slots, SyncedBlockEntity be, boolean stackNonStackables, int stackSize) {
			super(slots);
			this.blockEntity = be;
			this.stackNonStackables = stackNonStackables;
			this.stackSize = stackSize;
		}

		@Override
		protected void onContentsChanged(int slot) {
			super.onContentsChanged(slot);
			if (updateCallback != null)
				updateCallback.accept(slot);
			blockEntity.notifyUpdate();
		}

		@Override
		public int getSlotLimit(int slot) {
			return Math.min(stackNonStackables ? 64 : super.getSlotLimit(slot), stackSize);
		}

		public void whenContentsChange(Consumer<Integer> updateCallback) {
			this.updateCallback = updateCallback;
		}

	}

}
