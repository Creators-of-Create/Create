package com.simibubi.create.modules.logistics.item.filter;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

public abstract class AbstractFilterContainer extends Container {

	public PlayerEntity player;
	protected PlayerInventory playerInventory;
	public ItemStack filterItem;
	public ItemStackHandler filterInventory;

	protected AbstractFilterContainer(ContainerType<?> type, int id, PlayerInventory inv, PacketBuffer extraData) {
		this(type, id, inv, extraData.readItemStack());
	}

	protected AbstractFilterContainer(ContainerType<?> type, int id, PlayerInventory inv, ItemStack filterItem) {
		super(type, id);
		player = inv.player;
		playerInventory = inv;
		this.filterItem = filterItem;
		init();
	}

	protected void init() {
		this.filterInventory = createFilterInventory();
		readData(filterItem);
		addPlayerSlots();
		addFilterSlots();
		detectAndSendChanges();
	}

	protected void clearContents() {
		for (int i = 0; i < filterInventory.getSlots(); i++)
			filterInventory.setStackInSlot(i, ItemStack.EMPTY);
	}

	protected abstract int getInventoryOffset();

	protected abstract void addFilterSlots();

	protected abstract ItemStackHandler createFilterInventory();

	protected abstract void readData(ItemStack filterItem);

	protected abstract void saveData(ItemStack filterItem);

	protected void addPlayerSlots() {
		int x = 58;
		int y = 28 + getInventoryOffset();

		for (int hotbarSlot = 0; hotbarSlot < 9; ++hotbarSlot)
			this.addSlot(new Slot(playerInventory, hotbarSlot, x + hotbarSlot * 18, y + 58));
		for (int row = 0; row < 3; ++row)
			for (int col = 0; col < 9; ++col)
				this.addSlot(new Slot(playerInventory, col + row * 9 + 9, x + col * 18, y + row * 18));
	}

	@Override
	public boolean canMergeSlot(ItemStack stack, Slot slotIn) {
		return canDragIntoSlot(slotIn);
	}

	@Override
	public boolean canDragIntoSlot(Slot slotIn) {
		return slotIn.inventory == playerInventory;
	}

	@Override
	public boolean canInteractWith(PlayerEntity playerIn) {
		return true;
	}

	@Override
	public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, PlayerEntity player) {
		if (slotId == playerInventory.currentItem && clickTypeIn != ClickType.THROW)
			return ItemStack.EMPTY;

		ItemStack held = playerInventory.getItemStack();
		if (slotId < 36)
			return super.slotClick(slotId, dragType, clickTypeIn, player);
		if (clickTypeIn == ClickType.THROW)
			return ItemStack.EMPTY;

		int slot = slotId - 36;
		if (clickTypeIn == ClickType.CLONE) {
			if (player.isCreative() && held.isEmpty()) {
				ItemStack stackInSlot = filterInventory.getStackInSlot(slot).copy();
				stackInSlot.setCount(64);
				playerInventory.setItemStack(stackInSlot);
				return ItemStack.EMPTY;
			}
			return ItemStack.EMPTY;
		}

		if (held.isEmpty()) {
			filterInventory.setStackInSlot(slot, ItemStack.EMPTY);
			return ItemStack.EMPTY;
		}

		ItemStack insert = held.copy();
		insert.setCount(1);
		filterInventory.setStackInSlot(slot, insert);
		return held;
	}

	@Override
	public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
		if (index < 36) {
			ItemStack stackToInsert = playerInventory.getStackInSlot(index);
			for (int i = 0; i < filterInventory.getSlots(); i++) {
				ItemStack stack = filterInventory.getStackInSlot(i);
				if (ItemHandlerHelper.canItemStacksStack(stack, stackToInsert))
					break;
				if (stack.isEmpty()) {
					ItemStack copy = stackToInsert.copy();
					copy.setCount(1);
					filterInventory.insertItem(i, copy, false);
					break;
				}
			}
		} else
			filterInventory.extractItem(index - 36, 1, false);
		return ItemStack.EMPTY;
	}

	@Override
	public void onContainerClosed(PlayerEntity playerIn) {
		super.onContainerClosed(playerIn);
		filterItem.getOrCreateTag().put("Items", filterInventory.serializeNBT());
		saveData(filterItem);
	}

}
