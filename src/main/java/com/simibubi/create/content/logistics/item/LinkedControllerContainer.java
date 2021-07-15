package com.simibubi.create.content.logistics.item;

import com.simibubi.create.AllContainerTypes;
import com.simibubi.create.foundation.gui.IClearableContainer;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

public class LinkedControllerContainer extends Container implements IClearableContainer {

	public PlayerEntity player;
	protected PlayerInventory playerInventory;
	public ItemStack mainItem;
	public ItemStackHandler filterInventory;

	public LinkedControllerContainer(ContainerType<?> type, int id, PlayerInventory inv, PacketBuffer extraData) {
		this(type, id, inv, extraData.readItem());
	}

	public LinkedControllerContainer(ContainerType<?> type, int id, PlayerInventory inv, ItemStack filterItem) {
		super(type, id);
		player = inv.player;
		playerInventory = inv;
		this.mainItem = filterItem;
		init();
	}

	public static LinkedControllerContainer create(int id, PlayerInventory inv, ItemStack filterItem) {
		return new LinkedControllerContainer(AllContainerTypes.LINKED_CONTROLLER.get(), id, inv, filterItem);
	}

	protected void init() {
		this.filterInventory = createFilterInventory();
//		readData(mainItem);
		addPlayerSlots();
		addLinkSlots();
		broadcastChanges();
	}

	protected void addPlayerSlots() {
		int x = 8;
		int y = 131;

		for (int hotbarSlot = 0; hotbarSlot < 9; ++hotbarSlot)
			this.addSlot(new Slot(playerInventory, hotbarSlot, x + hotbarSlot * 18, y + 58));
		for (int row = 0; row < 3; ++row)
			for (int col = 0; col < 9; ++col)
				this.addSlot(new Slot(playerInventory, col + row * 9 + 9, x + col * 18, y + row * 18));
	}

	protected void addLinkSlots() {
		int x = 12;
		int y = 34;
		int slot = 0;
		
		for (int column = 0; column < 6; column++) {
			for (int row = 0; row < 2; ++row)
				addSlot(new SlotItemHandler(filterInventory, slot++, x, y + row * 18));
			x += 24;
			if (column == 3)
				x += 11;
		}
	}

	@Override
	public void clearContents() {
		for (int i = 0; i < filterInventory.getSlots(); i++)
			filterInventory.setStackInSlot(i, ItemStack.EMPTY);
	}

	@Override
	public boolean canTakeItemForPickAll(ItemStack stack, Slot slotIn) {
		return canDragTo(slotIn);
	}

	@Override
	public boolean canDragTo(Slot slotIn) {
		return slotIn.container == playerInventory;
	}

	@Override
	public boolean stillValid(PlayerEntity playerIn) {
		return playerInventory.getSelected() == mainItem;
	}

	@Override
	public ItemStack clicked(int slotId, int dragType, ClickType clickTypeIn, PlayerEntity player) {
		if (slotId == playerInventory.selected && clickTypeIn != ClickType.THROW)
			return ItemStack.EMPTY;

		ItemStack held = playerInventory.getCarried();
		if (slotId < 36)
			return super.clicked(slotId, dragType, clickTypeIn, player);
		if (clickTypeIn == ClickType.THROW)
			return ItemStack.EMPTY;

		int slot = slotId - 36;
		if (clickTypeIn == ClickType.CLONE) {
			if (player.isCreative() && held.isEmpty()) {
				ItemStack stackInSlot = filterInventory.getStackInSlot(slot)
					.copy();
				stackInSlot.setCount(64);
				playerInventory.setCarried(stackInSlot);
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

	protected ItemStackHandler createFilterInventory() {
		return LinkedControllerItem.getFrequencyItems(mainItem);
	}

	@Override
	public ItemStack quickMoveStack(PlayerEntity playerIn, int index) {
		if (index < 36) {
			ItemStack stackToInsert = playerInventory.getItem(index);
			for (int i = 0; i < filterInventory.getSlots(); i++) {
				ItemStack stack = filterInventory.getStackInSlot(i);
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
	public void removed(PlayerEntity playerIn) {
		super.removed(playerIn);
		mainItem.getOrCreateTag()
			.put("Items", filterInventory.serializeNBT());
//		saveData(filterItem);
	}

}
