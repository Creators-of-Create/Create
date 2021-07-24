package com.simibubi.create.content.contraptions.relays.encased;

import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;

public class ClutchTileEntity extends SplitShaftTileEntity {

	public ClutchTileEntity(TileEntityType<? extends ClutchTileEntity> type) {
		super(type);
	}

	@Override
	public float getRotationSpeedModifier(Direction face) {
		if (hasSource()) {
			if (face != getSourceFacing() && getBlockState().getValue(BlockStateProperties.POWERED))
				return 0;
		}
		return 1;
	}

}
