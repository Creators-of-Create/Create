package com.simibubi.create.content.contraptions.relays.gearbox;

import com.simibubi.create.content.contraptions.relays.encased.DirectionalShaftHalvesTileEntity;

import net.minecraft.tileentity.TileEntityType;

public class GearboxTileEntity extends DirectionalShaftHalvesTileEntity {

	public GearboxTileEntity(TileEntityType<? extends GearboxTileEntity> type) {
		super(type);
	}
	
	@Override
	protected boolean isNoisy() {
		return false;
	}
	
}
