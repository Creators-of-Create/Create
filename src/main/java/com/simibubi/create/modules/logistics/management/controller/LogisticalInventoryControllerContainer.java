package com.simibubi.create.modules.logistics.management.controller;

import com.simibubi.create.AllContainers;
import com.simibubi.create.foundation.block.AbstractTileEntityContainer;
import com.simibubi.create.foundation.type.CombinedCountedItemsList;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.items.SlotItemHandler;

public class LogisticalInventoryControllerContainer
		extends AbstractTileEntityContainer<LogisticalInventoryControllerTileEntity> {

	public CombinedCountedItemsList<String> allItems = new CombinedCountedItemsList<>();

	public LogisticalInventoryControllerContainer(int id, PlayerInventory inv, PacketBuffer extraData) {
		super(AllContainers.LOGISTICAL_CONTROLLER, id, inv, extraData);
	}

	public LogisticalInventoryControllerContainer(int id, PlayerInventory inv,
			LogisticalInventoryControllerTileEntity te) {
		super(AllContainers.LOGISTICAL_CONTROLLER, id, inv, te);
	}

	public void init() {
		addSlot(new SlotItemHandler(te.getInventory(), 0, 135, 32));
		addSlot(new SlotItemHandler(te.getInventory(), 1, 135 + 18, 32));
		addSlot(new SlotItemHandler(te.getInventory(), 2, 84, 29));
		addPlayerSlots(48, 118);
		detectAndSendChanges();
	}
	
	@Override
	public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, PlayerEntity player) {
		if (slotId != 2) 
			return super.slotClick(slotId, dragType, clickTypeIn, player);
		ItemStack item = player.inventory.getItemStack();
		ItemStack copy = item.copy();
		
		if (copy.isEmpty()) {
			te.getInventory().extractItem(2, 1, false);
			return ItemStack.EMPTY;
		}

		copy.setCount(1);
		te.getInventory().extractItem(2, 1, false);
		te.getInventory().insertItem(2, copy, false);
		
		return item;
	}

}
