package com.simibubi.create.modules.contraptions.receivers;

import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RecipeWrapper;

public class ProcessingInventory extends RecipeWrapper implements IItemHandler {
	protected int remainingTime;
	protected int recipeDuration;
	protected boolean appliedRecipe;

	public ProcessingInventory() {
		super(new ItemStackHandler(10));
	}

	@Override
	public void clear() {
		super.clear();
		remainingTime = 0;
		recipeDuration = 0;
		appliedRecipe = false;
	}

	public void write(CompoundNBT nbt) {
		NonNullList<ItemStack> stacks = NonNullList.create();
		for (int slot = 0; slot < inv.getSlots(); slot++) {
			ItemStack stack = inv.getStackInSlot(slot);
			stacks.add(stack);
		}
		ItemStackHelper.saveAllItems(nbt, stacks);
		nbt.putInt("ProcessingTime", remainingTime);
		nbt.putInt("RecipeTime", recipeDuration);
		nbt.putBoolean("AppliedRecipe", appliedRecipe);
	}

	public static ProcessingInventory read(CompoundNBT nbt) {
		ProcessingInventory inventory = new ProcessingInventory();
		NonNullList<ItemStack> stacks = NonNullList.withSize(10, ItemStack.EMPTY);
		ItemStackHelper.loadAllItems(nbt, stacks);

		for (int slot = 0; slot < stacks.size(); slot++)
			inventory.setInventorySlotContents(slot, stacks.get(slot));
		inventory.remainingTime = nbt.getInt("ProcessingTime");
		inventory.recipeDuration = nbt.getInt("RecipeTime");
		inventory.appliedRecipe = nbt.getBoolean("AppliedRecipe");

		return inventory;
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	public ItemStackHandler getItems() {
		return (ItemStackHandler) inv;
	}

	@Override
	public int getSlots() {
		return 9;
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
		if (!isItemValid(slot, stack))
			return stack;
		return inv.insertItem(slot, stack, simulate);
	}

	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		return ItemStack.EMPTY;
	}

	@Override
	public int getSlotLimit(int slot) {
		return 64;
	}

	@Override
	public boolean isItemValid(int slot, ItemStack stack) {
		return slot == 0 && isEmpty();
	}

}