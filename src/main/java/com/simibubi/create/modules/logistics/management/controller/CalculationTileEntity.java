package com.simibubi.create.modules.logistics.management.controller;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.modules.logistics.management.base.LogisticalInventoryControllerTileEntity;

public class CalculationTileEntity extends LogisticalInventoryControllerTileEntity {

	public CalculationTileEntity() {
		super(AllTileEntities.LOGISTICAL_CALCULATION_CONTROLLER.type);
	}

	@Override
	protected ShippingInventory createInventory() {
		return new ShippingInventory(false, false);
	}

}
