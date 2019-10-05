package com.simibubi.create.modules.logistics.management.index;

import com.simibubi.create.AllContainers;
import com.simibubi.create.foundation.type.CombinedCountedItemsList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

public class LogisticalIndexContainer extends Container {

	public LogisticalIndexTileEntity te;
	public CombinedCountedItemsList<String> allItems = new CombinedCountedItemsList<>();

	public LogisticalIndexContainer(int id, PlayerInventory inv, PacketBuffer extraData) {
		super(AllContainers.LOGISTICAL_INDEX.type, id);
		ClientWorld world = Minecraft.getInstance().world;
		this.te = (LogisticalIndexTileEntity) world.getTileEntity(extraData.readBlockPos());
		this.te.handleUpdateTag(extraData.readCompoundTag());
		init();
	}

	public LogisticalIndexContainer(int id, PlayerInventory inv, LogisticalIndexTileEntity te) {
		super(AllContainers.LOGISTICAL_INDEX.type, id);
		this.te = te;
		init();
		te.addPlayer((ServerPlayerEntity) inv.player);
	}

	private void init() {
		detectAndSendChanges();
	}
	
	@Override
	public void onContainerClosed(PlayerEntity playerIn) {
		if (!te.getWorld().isRemote)
			te.removePlayer((ServerPlayerEntity) playerIn);
		else 
			te.controllers.clear();
		super.onContainerClosed(playerIn);
	}

	@Override
	public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean canInteractWith(PlayerEntity playerIn) {
		return true;
	}
	
	public void refresh() {
		allItems.clear();
		te.controllers.forEach(allItems::add);
	}

}
