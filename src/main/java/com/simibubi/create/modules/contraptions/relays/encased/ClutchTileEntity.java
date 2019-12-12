package com.simibubi.create.modules.contraptions.relays.encased;

import com.simibubi.create.AllTileEntities;

import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;

public class ClutchTileEntity extends SplitShaftTileEntity {

	public ClutchTileEntity() {
		super(AllTileEntities.CLUTCH.type);
	}

	@Override
	public float getRotationSpeedModifier(Direction face) {
		if (hasSource()) {
			if (face != getSourceFacing() && getBlockState().get(BlockStateProperties.POWERED))
				return 0;
		}
		return 1;
	}

}
