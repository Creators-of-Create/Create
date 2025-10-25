package com.simibubi.create.content.trains.schedule;

import com.simibubi.create.foundation.gui.menu.GhostItemMenu;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

public class ScheduleMenu extends GhostItemMenu<ItemStack> {

	public boolean slotsActive = true;
	public int targetSlotsActive = 1;

	static final int slots = 2;

	public ScheduleMenu(MenuType<?> type, int id, Inventory inv, FriendlyByteBuf extraData) {
		super(type, id, inv, extraData);
	}

	public ScheduleMenu(MenuType<?> type, int id, Inventory inv, ItemStack contentHolder) {
		super(type, id, inv, contentHolder);
	}

	@Override
	protected ItemStackHandler createGhostInventory() {
		return new ItemStackHandler(slots);
	}

	@Override
	public void clicked(int index, int dragType, ClickType clickType, Player player) {
		if (!this.isInSlot(index) || clickType == ClickType.THROW || clickType == ClickType.CLONE) {
			super.clicked(index, dragType, clickType, player);
		}
	}

	@Override
	protected boolean allowRepeats() {
		return true;
	}

	@Override
	protected ItemStack createOnClient(FriendlyByteBuf extraData) {
		return extraData.readItem();
	}

	@Override
	protected void addSlots() {
		addPlayerSlots(46, 140);
		for (int i = 0; i < slots; i++)
			addSlot(new InactiveItemHandlerSlot(ghostInventory, i, i, 54 + 20 * i, 88));
	}

	@Override
	protected Slot createPlayerSlot(Inventory inventory, int index, int x, int y) {
		return new InactiveSlot(inventory, index, x, y);
	}

	@Override
	protected void saveData(ItemStack contentHolder) {}

	@Override
	public boolean stillValid(Player player) {
		return playerInventory.getSelected() == contentHolder;
	}

	@Override
	public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
		// prevent pick-all from taking this schedule out of its slot
		return super.canTakeItemForPickAll(stack, slot) && !this.isInSlot(slot.index);
	}

	protected boolean isInSlot(int index) {
		// Inventory has the hotbar as 0-8, but menus put the hotbar at 27-35
		return index >= 27 && index - 27 == playerInventory.selected;
	}

	class InactiveSlot extends Slot {

		public InactiveSlot(Container pContainer, int pIndex, int pX, int pY) {
			super(pContainer, pIndex, pX, pY);
		}

		@Override
		public boolean isActive() {
			return slotsActive;
		}

	}

	class InactiveItemHandlerSlot extends SlotItemHandler {

		private int targetIndex;

		public InactiveItemHandlerSlot(IItemHandler itemHandler, int targetIndex, int index, int xPosition,
			int yPosition) {
			super(itemHandler, index, xPosition, yPosition);
			this.targetIndex = targetIndex;
		}

		@Override
		public boolean isActive() {
			return slotsActive && targetIndex < targetSlotsActive;
		}

	}

}
