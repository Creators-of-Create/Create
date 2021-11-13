package com.simibubi.create.lib.utility;

import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

public interface MenuSupplierWrapper<T extends AbstractContainerMenu> extends MenuType.MenuSupplier<T>, ScreenHandlerRegistry.SimpleClientHandlerFactory<T> {
	T create(int windowId, Inventory inv, FriendlyByteBuf data);

	@Override
	default T create(int syncId, Inventory playerInventory) {
		return create(syncId, playerInventory, null);
	}
}
