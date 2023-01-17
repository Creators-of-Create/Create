package com.simibubi.create.foundation.gui.menu;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

public abstract class GhostItemMenu<T> extends MenuBase<T> implements IClearableMenu {

	public ItemStackHandler ghostInventory;

	protected GhostItemMenu(MenuType<?> type, int id, Inventory inv, FriendlyByteBuf extraData) {
		super(type, id, inv, extraData);
	}

	protected GhostItemMenu(MenuType<?> type, int id, Inventory inv, T contentHolder) {
		super(type, id, inv, contentHolder);
	}

	protected abstract ItemStackHandler createGhostInventory();

	protected abstract boolean allowRepeats();

	@Override
	protected void initAndReadInventory(T contentHolder) {
		ghostInventory = createGhostInventory();
	}

	@Override
	public void clearContents() {
		for (int i = 0; i < ghostInventory.getSlots(); i++)
			ghostInventory.setStackInSlot(i, ItemStack.EMPTY);
	}

	@Override
	public boolean canTakeItemForPickAll(ItemStack stack, Slot slotIn) {
		return slotIn.container == playerInventory;
	}

	@Override
	public boolean canDragTo(Slot slotIn) {
		if (allowRepeats())
			return true;
		return slotIn.container == playerInventory;
	}

	@Override
	public void clicked(int slotId, int dragType, ClickType clickTypeIn, Player player) {
		if (slotId < 36) {
			super.clicked(slotId, dragType, clickTypeIn, player);
			return;
		}
		if (clickTypeIn == ClickType.THROW)
			return;

		ItemStack held = getCarried();
		int slot = slotId - 36;
		if (clickTypeIn == ClickType.CLONE) {
			if (player.isCreative() && held.isEmpty()) {
				ItemStack stackInSlot = ghostInventory.getStackInSlot(slot)
						.copy();
				stackInSlot.setCount(stackInSlot.getMaxStackSize());
				setCarried(stackInSlot);
				return;
			}
			return;
		}

		ItemStack insert;
		if (held.isEmpty()) {
			insert = ItemStack.EMPTY;
		} else {
			insert = held.copy();
			insert.setCount(1);
		}
		ghostInventory.setStackInSlot(slot, insert);
		getSlot(slotId).setChanged();
	}

	@Override
	public ItemStack quickMoveStack(Player playerIn, int index) {
		if (index < 36) {
			ItemStack stackToInsert = playerInventory.getItem(index);
			for (int i = 0; i < ghostInventory.getSlots(); i++) {
				ItemStack stack = ghostInventory.getStackInSlot(i);
				if (!allowRepeats() && ItemHandlerHelper.canItemStacksStack(stack, stackToInsert))
					break;
				if (stack.isEmpty()) {
					ItemStack copy = stackToInsert.copy();
					copy.setCount(1);
					ghostInventory.insertItem(i, copy, false);
					getSlot(i + 36).setChanged();
					break;
				}
			}
		} else {
			ghostInventory.extractItem(index - 36, 1, false);
			getSlot(index).setChanged();
		}
		return ItemStack.EMPTY;
	}

}
