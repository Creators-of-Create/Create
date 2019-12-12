package com.simibubi.create.modules.contraptions.components.actors;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.foundation.block.SyncedTileEntity;

public class HarvesterTileEntity extends SyncedTileEntity {

	public HarvesterTileEntity() {
		super(AllTileEntities.HARVESTER.type);
	}
	
	@Override
	public boolean hasFastRenderer() {
		return true;
	} 

}
