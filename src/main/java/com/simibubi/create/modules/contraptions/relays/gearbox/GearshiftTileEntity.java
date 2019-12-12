package com.simibubi.create.modules.contraptions.relays.gearbox;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.modules.contraptions.relays.encased.SplitShaftTileEntity;

import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;

public class GearshiftTileEntity extends SplitShaftTileEntity {

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
