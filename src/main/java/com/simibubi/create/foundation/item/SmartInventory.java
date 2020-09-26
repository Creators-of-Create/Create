package com.simibubi.create.foundation.item;

import javax.annotation.Nonnull;

import com.simibubi.create.foundation.tileEntity.SyncedTileEntity;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RecipeWrapper;

public class SmartInventory extends RecipeWrapper implements IItemHandlerModifiableIntermediate, INBTSerializable<CompoundNBT> {

	private boolean extractionAllowed;
	private boolean insertionAllowed;
	private int stackSize;

	public SmartInventory(int slots, SyncedTileEntity te) {
		super(new SyncedStackHandler(slots, te));
		insertionAllowed = true;
		extractionAllowed = true;
		stackSize = 64;
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

	public SmartInventory withMaxStackSize(int stackSize) {
		this.stackSize = stackSize;
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
	public ItemStack getStackInSlot(int slot) {
		return super.getStackInSlot(slot);
	}
	
	public int getStackLimit(int slot, @Nonnull ItemStack stack) {
		return Math.min(getSlotLimit(slot), stack.getMaxStackSize());
	}

	@Override
	public CompoundNBT serializeNBT() {
		return getInv().serializeNBT();
	}

	@Override
	public void deserializeNBT(CompoundNBT nbt) {
		getInv().deserializeNBT(nbt);
	}
	
	private SyncedStackHandler getInv() {
		return (SyncedStackHandler) inv;
	}

	private static class SyncedStackHandler extends ItemStackHandler {

		private SyncedTileEntity te;

		public SyncedStackHandler(int slots, SyncedTileEntity te) {
			super(slots);
			this.te = te;
		}

		@Override
		protected void onContentsChanged(int slot) {
			super.onContentsChanged(slot);
			te.notifyUpdate();
		}

	}

	@Override
	public ItemStack getStackInSlotIntermediate(int slot) {
		return getStackInSlot(slot);
	}

}
