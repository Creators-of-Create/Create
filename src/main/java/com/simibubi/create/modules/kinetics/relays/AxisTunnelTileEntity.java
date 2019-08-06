package com.simibubi.create.modules.kinetics.relays;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.modules.kinetics.base.KineticTileEntity;

public class AxisTunnelTileEntity extends KineticTileEntity {

	public AxisTunnelTileEntity() {
		super(AllTileEntities.AXIS_TUNNEL.type);
	}
	
	@Override
	public boolean hasFastRenderer() {
		return true;
	}

}
