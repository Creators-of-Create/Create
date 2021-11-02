package com.simibubi.create.foundation.gui;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

public abstract class GhostItemContainer<T> extends ContainerBase<T> implements IClearableContainer {

	public ItemStackHandler ghostInventory;

	protected GhostItemContainer(ContainerType<?> type, int id, PlayerInventory inv, PacketBuffer extraData) {
		super(type, id, inv, extraData);
	}

	protected GhostItemContainer(ContainerType<?> type, int id, PlayerInventory inv, T contentHolder) {
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
	public ItemStack clicked(int slotId, int dragType, ClickType clickTypeIn, PlayerEntity player) {
		ItemStack held = playerInventory.getCarried();
		if (slotId < 36)
			return super.clicked(slotId, dragType, clickTypeIn, player);
		if (clickTypeIn == ClickType.THROW)
			return ItemStack.EMPTY;

		int slot = slotId - 36;
		if (clickTypeIn == ClickType.CLONE) {
			if (player.isCreative() && held.isEmpty()) {
				ItemStack stackInSlot = ghostInventory.getStackInSlot(slot)
						.copy();
				stackInSlot.setCount(stackInSlot.getMaxStackSize());
				playerInventory.setCarried(stackInSlot);
				return ItemStack.EMPTY;
			}
			return ItemStack.EMPTY;
		}

		if (held.isEmpty()) {
			ghostInventory.setStackInSlot(slot, ItemStack.EMPTY);
			getSlot(slotId).setChanged();
			return ItemStack.EMPTY;
		}

		ItemStack insert = held.copy();
		insert.setCount(1);
		ghostInventory.setStackInSlot(slot, insert);
		getSlot(slotId).setChanged();
		return held;
	}

	@Override
	public ItemStack quickMoveStack(PlayerEntity playerIn, int index) {
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
