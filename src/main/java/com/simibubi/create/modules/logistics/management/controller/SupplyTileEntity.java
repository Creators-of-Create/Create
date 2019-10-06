package com.simibubi.create.modules.logistics.management.controller;

import com.simibubi.create.AllTileEntities;

public class SupplyTileEntity extends LogisticalInventoryControllerTileEntity {

	public SupplyTileEntity() {
		super(AllTileEntities.LOGISTICAL_SUPPLY_CONTROLLER.type);
	}

	@Override
	protected ShippingInventory createInventory() {
		return new ShippingInventory(true, false);
	}
	
	@Override
	public boolean isSupplier() {
		return true;
	}
	
}
