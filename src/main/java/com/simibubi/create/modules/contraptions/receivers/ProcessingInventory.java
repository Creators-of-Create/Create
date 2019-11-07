package com.simibubi.create.modules.contraptions.receivers;

import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RecipeWrapper;

public class ProcessingInventory extends RecipeWrapper {
	protected int processingDuration;
	protected boolean appliedRecipe;

	public ProcessingInventory() {
		super(new ItemStackHandler(10));
	}

	@Override
	public void clear() {
		super.clear();
		processingDuration = 0;
		appliedRecipe = false;
	}

	public void write(CompoundNBT nbt) {
		NonNullList<ItemStack> stacks = NonNullList.create();
		for (int slot = 0; slot < inv.getSlots(); slot++) {
			ItemStack stack = inv.getStackInSlot(slot);
			stacks.add(stack);
		}
		ItemStackHelper.saveAllItems(nbt, stacks);
		nbt.putInt("ProcessingTime", processingDuration);
		nbt.putBoolean("AppliedRecipe", appliedRecipe);
	}

	public static ProcessingInventory read(CompoundNBT nbt) {
		ProcessingInventory inventory = new ProcessingInventory();
		NonNullList<ItemStack> stacks = NonNullList.withSize(10, ItemStack.EMPTY);
		ItemStackHelper.loadAllItems(nbt, stacks);

		for (int slot = 0; slot < stacks.size(); slot++)
			inventory.setInventorySlotContents(slot, stacks.get(slot));
		inventory.processingDuration = nbt.getInt("ProcessingTime");
		inventory.appliedRecipe = nbt.getBoolean("AppliedRecipe");

		return inventory;
	}

	public ItemStackHandler getItems() {
		return (ItemStackHandler) inv;
	}

}