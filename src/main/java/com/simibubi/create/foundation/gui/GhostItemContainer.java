package com.simibubi.create.foundation.gui;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

public abstract class GhostItemContainer<T> extends Container implements IClearableContainer {

	public PlayerEntity player;
	public PlayerInventory playerInventory;
	public ItemStackHandler ghostInventory;
	public T contentHolder;

	protected GhostItemContainer(ContainerType<?> type, int id, PlayerInventory inv, PacketBuffer extraData) {
		super(type, id);
		init(inv, createOnClient(extraData));
	}

	protected GhostItemContainer(ContainerType<?> type, int id, PlayerInventory inv, T contentHolder) {
		super(type, id);
		init(inv, contentHolder);
	}

	@OnlyIn(Dist.CLIENT)
	protected abstract T createOnClient(PacketBuffer extraData);

	protected abstract void addSlots();

	protected abstract ItemStackHandler createGhostInventory();

	protected abstract void readData(T contentHolder);

	protected abstract void saveData(T contentHolder);

	protected abstract boolean allowRepeats();

	protected void init(PlayerInventory inv, T contentHolder) {
		player = inv.player;
		playerInventory = inv;
		this.contentHolder = contentHolder;
		ghostInventory = createGhostInventory();
		readData(contentHolder);
		addSlots();
		detectAndSendChanges();
	}

	@Override
	public void clearContents() {
		for (int i = 0; i < ghostInventory.getSlots(); i++)
			ghostInventory.setStackInSlot(i, ItemStack.EMPTY);
	}

	protected void addPlayerSlots(int x, int y) {
		for (int hotbarSlot = 0; hotbarSlot < 9; ++hotbarSlot)
			this.addSlot(new Slot(playerInventory, hotbarSlot, x + hotbarSlot * 18, y + 58));
		for (int row = 0; row < 3; ++row)
			for (int col = 0; col < 9; ++col)
				this.addSlot(new Slot(playerInventory, col + row * 9 + 9, x + col * 18, y + row * 18));
	}

	@Override
	public boolean canMergeSlot(ItemStack stack, Slot slotIn) {
		return slotIn.inventory == playerInventory;
	}

	@Override
	public boolean canDragIntoSlot(Slot slotIn) {
		if (allowRepeats())
			return true;
		return slotIn.inventory == playerInventory;
	}

	@Override
	public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, PlayerEntity player) {
		ItemStack held = playerInventory.getItemStack();
		if (slotId < 36)
			return super.slotClick(slotId, dragType, clickTypeIn, player);
		if (clickTypeIn == ClickType.THROW)
			return ItemStack.EMPTY;

		int slot = slotId - 36;
		if (clickTypeIn == ClickType.CLONE) {
			if (player.isCreative() && held.isEmpty()) {
				ItemStack stackInSlot = ghostInventory.getStackInSlot(slot)
						.copy();
				stackInSlot.setCount(stackInSlot.getMaxStackSize());
				playerInventory.setItemStack(stackInSlot);
				return ItemStack.EMPTY;
			}
			return ItemStack.EMPTY;
		}

		if (held.isEmpty()) {
			ghostInventory.setStackInSlot(slot, ItemStack.EMPTY);
			getSlot(slotId).onSlotChanged();
			return ItemStack.EMPTY;
		}

		ItemStack insert = held.copy();
		insert.setCount(1);
		ghostInventory.setStackInSlot(slot, insert);
		getSlot(slotId).onSlotChanged();
		return held;
	}

	@Override
	public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
		if (index < 36) {
			ItemStack stackToInsert = playerInventory.getStackInSlot(index);
			for (int i = 0; i < ghostInventory.getSlots(); i++) {
				ItemStack stack = ghostInventory.getStackInSlot(i);
				if (!allowRepeats() && ItemHandlerHelper.canItemStacksStack(stack, stackToInsert))
					break;
				if (stack.isEmpty()) {
					ItemStack copy = stackToInsert.copy();
					copy.setCount(1);
					ghostInventory.insertItem(i, copy, false);
					getSlot(i + 36).onSlotChanged();
					break;
				}
			}
		} else {
			ghostInventory.extractItem(index - 36, 1, false);
			getSlot(index).onSlotChanged();
		}
		return ItemStack.EMPTY;
	}

	@Override
	public void onContainerClosed(PlayerEntity playerIn) {
		super.onContainerClosed(playerIn);
		saveData(contentHolder);
	}

}
