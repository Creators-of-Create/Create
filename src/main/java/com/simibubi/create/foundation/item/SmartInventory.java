package com.simibubi.create.foundation.item;

import java.util.function.BiPredicate;
import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;

import com.simibubi.create.foundation.blockEntity.ItemHandlerContainer;
import com.simibubi.create.foundation.blockEntity.SyncedBlockEntity;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemStackHandler;

public class SmartInventory extends ItemHandlerContainer
	implements IItemHandlerModifiable, INBTSerializable<CompoundTag> {

	protected boolean extractionAllowed;
	protected boolean insertionAllowed;
	protected boolean stackNonStackables;
	protected SyncedStackHandler wrapped;
	protected int stackSize;

	public SmartInventory(int slots, SyncedBlockEntity be) {
		this(slots, be, 64, false);
	}

	public SmartInventory(int slots, SyncedBlockEntity be, BiPredicate<Integer, ItemStack> isValid) {
		this(slots, be, 64, false, isValid);
	}

	public SmartInventory(int slots, SyncedBlockEntity be, int stackSize, boolean stackNonStackables) {
		this(new SyncedStackHandler(slots, be, stackNonStackables, stackSize), stackSize, stackNonStackables);
	}

	public SmartInventory(int slots, SyncedBlockEntity be, int stackSize, boolean stackNonStackables, BiPredicate<Integer, ItemStack> isValid) {
		this(new SyncedStackHandler(slots, be, stackNonStackables, stackSize, isValid), stackSize, stackNonStackables);
	}

	public SmartInventory(IItemHandlerModifiable inv, int stackSize, boolean stackNonStackables) {
		super(inv);
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
		((SyncedStackHandler) inv).setStackInSlot(slot, stack);
	}

	public int getStackLimit(int slot, @NotNull ItemStack stack) {
		return Math.min(getSlotLimit(slot), stack.getMaxStackSize());
	}

	@Override
	public CompoundTag serializeNBT(HolderLookup.Provider registries) {
		return getInv().serializeNBT(registries);
	}

	@Override
	public void deserializeNBT(HolderLookup.Provider registries, CompoundTag nbt) {
		getInv().deserializeNBT(registries, nbt);
	}

	private SyncedStackHandler getInv() {
		return (SyncedStackHandler) inv;
	}

	protected static class SyncedStackHandler extends ItemStackHandler {

		private SyncedBlockEntity blockEntity;
		private boolean stackNonStackables;
		private int stackSize;
		private BiPredicate<Integer, ItemStack> isValid = super::isItemValid;
		private Consumer<Integer> updateCallback;

		public SyncedStackHandler(int slots, SyncedBlockEntity be, boolean stackNonStackables, int stackSize, BiPredicate<Integer, ItemStack> isValid) {
			this(slots, be, stackNonStackables, stackSize);
			this.isValid = isValid;
		}

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

		@Override
		public boolean isItemValid(int slot, @NotNull ItemStack stack) {
			return isValid.test(slot, stack);
		}

		public void whenContentsChange(Consumer<Integer> updateCallback) {
			this.updateCallback = updateCallback;
		}

	}

}
