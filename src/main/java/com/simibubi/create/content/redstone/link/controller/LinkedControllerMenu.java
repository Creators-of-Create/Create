package com.simibubi.create.content.redstone.link.controller;

import com.simibubi.create.AllDataComponents;
import com.simibubi.create.AllMenuTypes;
import com.simibubi.create.foundation.gui.menu.HeldItemGhostItemMenu;
import com.simibubi.create.foundation.item.ItemHelper;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public class LinkedControllerMenu extends HeldItemGhostItemMenu {

	public LinkedControllerMenu(MenuType<?> type, int id, Inventory inv, RegistryFriendlyByteBuf extraData) {
		super(type, id, inv, extraData);
	}

	public LinkedControllerMenu(MenuType<?> type, int id, Inventory inv, ItemStack filterItem) {
		super(type, id, inv, filterItem);
	}

	public static LinkedControllerMenu create(int id, Inventory inv, ItemStack filterItem) {
		return new LinkedControllerMenu(AllMenuTypes.LINKED_CONTROLLER.get(), id, inv, filterItem);
	}

	@Override
	protected ItemStackHandler createGhostInventory() {
		return LinkedControllerItem.getFrequencyItems(contentHolder);
	}

	@Override
	protected void addSlots() {
		addPlayerSlots(8, 131);

		int x = 12;
		int y = 34;
		int slot = 0;

		for (int column = 0; column < 6; column++) {
			for (int row = 0; row < 2; ++row)
				addSlot(new SlotItemHandler(ghostInventory, slot++, x, y + row * 18));
			x += 24;
			if (column == 3)
				x += 11;
		}
	}

	@Override
	protected void saveData(ItemStack contentHolder) {
		contentHolder.set(AllDataComponents.LINKED_CONTROLLER_ITEMS, ItemHelper.containerContentsFromHandler(ghostInventory));
	}

	@Override
	protected boolean allowRepeats() {
		return true;
	}

}
