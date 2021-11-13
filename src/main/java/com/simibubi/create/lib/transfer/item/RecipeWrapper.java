package com.simibubi.create.lib.transfer.item;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class RecipeWrapper implements Container {
	protected final IItemHandlerModifiable inv;

	public RecipeWrapper(IItemHandlerModifiable inv) {
		this.inv = inv;
	}

	@Override
	public int getContainerSize() {
		return inv.getSlots();
	}

	@Override
	public ItemStack getItem(int slot) {
		return inv.getStackInSlot(slot);
	}

	@Override
	public ItemStack removeItem(int slot, int count) {
		ItemStack stack = inv.getStackInSlot(slot);
		return stack.isEmpty() ? ItemStack.EMPTY : stack.split(count);
	}

	@Override
	public void setItem(int slot, ItemStack stack) {
		inv.setStackInSlot(slot, stack);
	}

	@Override
	public ItemStack removeItemNoUpdate(int slot) {
		ItemStack stack = getItem(slot);
		if (stack.isEmpty()) return ItemStack.EMPTY;
		setItem(slot, ItemStack.EMPTY);
		return stack;
	}

	@Override
	public boolean isEmpty() {
		for (int i = 0; i < getContainerSize(); i++) {
			if (inv.getStackInSlot(i).isEmpty()) continue;
			return false;
		}
		return true;
	}

	@Override
	public boolean canPlaceItem(int slot, ItemStack stack) {
		return inv.isItemValid(slot, stack);
	}

	@Override
	public void clearContent() {
		for (int i = 0; i < getContainerSize(); i++) {
			inv.setStackInSlot(i, ItemStack.EMPTY);
		}
	}

	@Override
	public int getMaxStackSize() { return 0; }
	@Override
	public void setChanged() {}
	@Override
	public boolean stillValid(Player player) { return false; }
	@Override
	public void startOpen(Player player) {}
	@Override
	public void stopOpen(Player player) {}
}
