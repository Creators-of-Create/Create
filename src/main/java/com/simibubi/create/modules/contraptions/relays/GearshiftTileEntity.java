package com.simibubi.create.modules.contraptions.relays;

import com.simibubi.create.AllTileEntities;

import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;

public class GearshiftTileEntity extends SidedAxisTunnelTileEntity {

	public GearshiftTileEntity() {
		super(AllTileEntities.GEARSHIFT.type);
	}

	@Override
	public float getRotationSpeedModifier(Direction face) {
		if (hasSource()) {
			if (face != getSourceFacing() && getBlockState().get(BlockStateProperties.POWERED))
				return -1;
		}
		return 1;
	}
	
}
