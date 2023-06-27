package com.simibubi.create.content.processing.recipe;

import java.util.function.Consumer;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

public class ProcessingInventory extends ItemStackHandler {
	public float remainingTime;
	public float recipeDuration;
	public boolean appliedRecipe;
	public Consumer<ItemStack> callback;
	private boolean limit;

	public ProcessingInventory(Consumer<ItemStack> callback) {
		super(16);
		this.callback = callback;
	}
	
	public ProcessingInventory withSlotLimit(boolean limit) {
		this.limit = limit;
		return this;
	}
	
	@Override
	public int getSlotLimit(int slot) {
		return !limit ? super.getSlotLimit(slot) : 1;
	}

	public void clear() {
		for (int i = 0; i < getSlots(); i++)
			setStackInSlot(i, ItemStack.EMPTY);
		remainingTime = 0;
		recipeDuration = 0;
		appliedRecipe = false;
	}

	public boolean isEmpty() {
		for (int i = 0; i < getSlots(); i++)
			if (!getStackInSlot(i).isEmpty())
				return false;
		return true;
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
		ItemStack insertItem = super.insertItem(slot, stack, simulate);
		if (slot == 0 && !insertItem.equals(stack, true))
			callback.accept(getStackInSlot(slot));
		return insertItem;
	}

	@Override
	public CompoundTag serializeNBT() {
		CompoundTag nbt = super.serializeNBT();
		nbt.putFloat("ProcessingTime", remainingTime);
		nbt.putFloat("RecipeTime", recipeDuration);
		nbt.putBoolean("AppliedRecipe", appliedRecipe);
		return nbt;
	}

	@Override
	public void deserializeNBT(CompoundTag nbt) {
		remainingTime = nbt.getFloat("ProcessingTime");
		recipeDuration = nbt.getFloat("RecipeTime");
		appliedRecipe = nbt.getBoolean("AppliedRecipe");
		super.deserializeNBT(nbt);
		if(isEmpty())
			appliedRecipe = false;
	}

	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean isItemValid(int slot, ItemStack stack) {
		return slot == 0 && isEmpty();
	}

}