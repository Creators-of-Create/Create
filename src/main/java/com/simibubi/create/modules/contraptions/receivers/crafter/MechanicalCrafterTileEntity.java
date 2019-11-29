package com.simibubi.create.modules.contraptions.receivers.crafter;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.modules.contraptions.base.KineticTileEntity;

public class MechanicalCrafterTileEntity extends KineticTileEntity {

	public MechanicalCrafterTileEntity() {
		super(AllTileEntities.MECHANICAL_CRAFTER.type);
	}
	
	@Override
	public boolean hasFastRenderer() {
		return false;
	}

}
