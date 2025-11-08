package com.simibubi.create.content.trains.schedule;

import com.simibubi.create.foundation.gui.menu.HeldItemGhostItemMenu;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public class ScheduleMenu extends HeldItemGhostItemMenu {

	public boolean slotsActive = true;
	public int targetSlotsActive = 1;

	static final int slots = 2;

	public ScheduleMenu(MenuType<?> type, int id, Inventory inv, RegistryFriendlyByteBuf extraData) {
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
	protected boolean allowRepeats() {
		return true;
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
