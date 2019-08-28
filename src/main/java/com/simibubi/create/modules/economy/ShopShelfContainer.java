package com.simibubi.create.modules.economy;

import com.simibubi.create.AllContainers;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;

public class ShopShelfContainer extends Container {

	public ShopShelfContainer(int id, PlayerInventory inv, ShopShelfTileEntity te) {
		super(AllContainers.SHOP_SHELF.type, id);
	}
	
	public ShopShelfContainer(int id, PlayerInventory inv, PacketBuffer extraData) {
		super(AllContainers.SHOP_SHELF.type, id);
	}

	@Override
	public boolean canInteractWith(PlayerEntity playerIn) {
		return true;
	}

}
