package com.simibubi.create.modules.contraptions.relays;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.modules.contraptions.base.KineticTileEntity;

public class AxisTunnelTileEntity extends KineticTileEntity {

	public AxisTunnelTileEntity() {
		super(AllTileEntities.AXIS_TUNNEL.type);
	}
	
	@Override
	public boolean hasFastRenderer() {
		return true;
	}

}
