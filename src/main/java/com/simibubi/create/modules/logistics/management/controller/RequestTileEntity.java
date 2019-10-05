package com.simibubi.create.modules.logistics.management.controller;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.modules.logistics.management.base.LogisticalInventoryControllerTileEntity;

public class RequestTileEntity extends LogisticalInventoryControllerTileEntity {

	public RequestTileEntity() {
		super(AllTileEntities.LOGISTICAL_REQUEST_CONTROLLER.type);
	}

	@Override
	protected ShippingInventory createInventory() {
		return new ShippingInventory(false, true);
	}

	@Override
	public void handleAdded() {
		if (world.isRemote)
			return;
		if (getNetwork() != null)
			return;
		super.handleAdded();
	}

	@Override
	public boolean isReceiver() {
		return true;
	}

}
