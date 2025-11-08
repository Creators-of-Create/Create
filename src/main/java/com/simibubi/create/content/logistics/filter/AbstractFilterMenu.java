package com.simibubi.create.content.logistics.filter;

import com.simibubi.create.AllDataComponents;
import com.simibubi.create.foundation.gui.menu.HeldItemGhostItemMenu;
import com.simibubi.create.foundation.item.ItemHelper;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

public abstract class AbstractFilterMenu extends HeldItemGhostItemMenu {

	protected AbstractFilterMenu(MenuType<?> type, int id, Inventory inv, RegistryFriendlyByteBuf extraData) {
		super(type, id, inv, extraData);
	}

	protected AbstractFilterMenu(MenuType<?> type, int id, Inventory inv, ItemStack contentHolder) {
		super(type, id, inv, contentHolder);
	}

	@Override
	protected boolean allowRepeats() {
		return false;
	}

	protected abstract int getPlayerInventoryXOffset();

	protected abstract int getPlayerInventoryYOffset();

	protected abstract void addFilterSlots();

	@Override
	protected void addSlots() {
		addPlayerSlots(getPlayerInventoryXOffset(), getPlayerInventoryYOffset());
		addFilterSlots();
	}

	@Override
	protected void saveData(ItemStack contentHolder) {
		for (int i = 0; i < ghostInventory.getSlots(); i++) {
			if (!ghostInventory.getStackInSlot(i).isEmpty()) {
				contentHolder.set(AllDataComponents.FILTER_ITEMS, ItemHelper.containerContentsFromHandler(ghostInventory));
				return;
			}
		}
		contentHolder.remove(AllDataComponents.FILTER_ITEMS);
	}

}
