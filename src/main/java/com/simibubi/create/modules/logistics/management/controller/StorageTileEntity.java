package com.simibubi.create.modules.logistics.management.controller;

import com.simibubi.create.AllTileEntities;

public class StorageTileEntity extends LogisticalInventoryControllerTileEntity {

	public StorageTileEntity() {
		super(AllTileEntities.LOGISTICAL_STORAGE_CONTROLLER.type);
	}
	
	@Override
	protected ShippingInventory createInventory() {
		return new ShippingInventory(true, true);
	}

	@Override
	public boolean isSupplier() {
		return true;
	}
	
	@Override
	public boolean isReceiver() {
		return true;
	}
	
}
