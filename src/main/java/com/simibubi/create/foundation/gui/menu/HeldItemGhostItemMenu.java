package com.simibubi.create.foundation.gui.menu;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import net.neoforged.api.distmarker.Dist;

/**
 * A {@link GhostItemMenu} that is linked to the item in a player's main hand. Prevents its owner item from being manipulated.
 */
public abstract class HeldItemGhostItemMenu extends GhostItemMenu<ItemStack> {
	protected HeldItemGhostItemMenu(MenuType<?> type, int id, Inventory inv, RegistryFriendlyByteBuf extraData) {
		super(type, id, inv, extraData);
	}

	protected HeldItemGhostItemMenu(MenuType<?> type, int id, Inventory inv, ItemStack contentHolder) {
		super(type, id, inv, contentHolder);
	}

	@Override
	protected ItemStack createOnClient(RegistryFriendlyByteBuf extraData) {
		return ItemStack.STREAM_CODEC.decode(extraData);
	}

	@Override
	public void clicked(int index, int dragType, ContainerInput clickType, Player player) {
		if (!this.isInSlot(index) || clickType == ContainerInput.THROW || clickType == ContainerInput.CLONE) {
			super.clicked(index, dragType, clickType, player);
		}
	}

	@Override
	public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
		// prevent pick-all from taking the owner item out of its slot
		return super.canTakeItemForPickAll(stack, slot) && !this.isInSlot(slot.index);
	}

	@Override
	public boolean stillValid(Player player) {
		return playerInventory.getSelectedItem() == contentHolder;
	}

	protected boolean isInSlot(int index) {
		// Inventory has the hotbar as 0-8, but menus put the hotbar at 27-35
		return index >= 27 && index - 27 == playerInventory.getSelectedSlot();
	}
}
