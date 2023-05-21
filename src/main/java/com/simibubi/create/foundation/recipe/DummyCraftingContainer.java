package com.simibubi.create.foundation.recipe;

import org.jetbrains.annotations.NotNull;

import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public class DummyCraftingContainer extends CraftingContainer {

	private final NonNullList<ItemStack> inv;

	public DummyCraftingContainer(IItemHandler itemHandler, int[] extractedItemsFromSlot) {
		super(null, 0, 0);

		this.inv = createInventory(itemHandler, extractedItemsFromSlot);
	}

	@Override
	public int getContainerSize() {
		return this.inv.size();
	}

	@Override
	public boolean isEmpty() {
		for (int slot = 0; slot < this.getContainerSize(); slot++) {
			if (!this.getItem(slot).isEmpty())
				return false;
		}

		return true;
	}

	@Override
	public @NotNull ItemStack getItem(int slot) {
		return slot >= this.getContainerSize() ? ItemStack.EMPTY : this.inv.get(slot);
	}

	@Override
	public @NotNull ItemStack removeItemNoUpdate(int slot) {
		return ItemStack.EMPTY;
	}

	@Override
	public @NotNull ItemStack removeItem(int slot, int count) {
		return ItemStack.EMPTY;
	}

	@Override
	public void setItem(int slot, @NotNull ItemStack stack) {}

	@Override
	public void clearContent() {}

	@Override
	public void fillStackedContents(@NotNull StackedContents helper) {}

	private static NonNullList<ItemStack> createInventory(IItemHandler itemHandler, int[] extractedItemsFromSlot) {
		NonNullList<ItemStack> inv = NonNullList.create();

		for (int slot = 0; slot < itemHandler.getSlots(); slot++) {
			ItemStack stack = itemHandler.getStackInSlot(slot);

			if (stack.isEmpty())
				continue;

			for (int i = 0; i < extractedItemsFromSlot[slot]; i++) {
				ItemStack stackCopy = stack.copy();
				stackCopy.setCount(1);

				inv.add(stackCopy);
			}
		}

		return inv;
	}

}
