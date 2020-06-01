package com.simibubi.create.content.contraptions.components.actors;

import com.simibubi.create.foundation.tileEntity.SyncedTileEntity;

import net.minecraft.tileentity.TileEntityType;

public class HarvesterTileEntity extends SyncedTileEntity {

	public HarvesterTileEntity(TileEntityType<? extends HarvesterTileEntity> type) {
		super(type);
	}
	
	@Override
	public boolean hasFastRenderer() {
		return true;
	} 

}
