package com.simibubi.create.content.contraptions.components.actors;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.foundation.tileEntity.SyncedTileEntity;

public class HarvesterTileEntity extends SyncedTileEntity {

	public HarvesterTileEntity() {
		super(AllTileEntities.HARVESTER.type);
	}
	
	@Override
	public boolean hasFastRenderer() {
		return true;
	} 

}
