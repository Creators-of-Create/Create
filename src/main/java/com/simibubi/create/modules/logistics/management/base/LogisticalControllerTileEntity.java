package com.simibubi.create.modules.logistics.management.base;

import net.minecraft.tileentity.TileEntityType;

public abstract class LogisticalControllerTileEntity extends LogisticalActorTileEntity {

	public LogisticalControllerTileEntity(TileEntityType<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
	}
	
	@Override
	public boolean hasFastRenderer() {
		return true;
	}

}
