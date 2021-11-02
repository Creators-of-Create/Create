package com.simibubi.create.content.contraptions.relays.gearbox;

import com.simibubi.create.content.contraptions.relays.encased.SplitShaftTileEntity;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class GearshiftTileEntity extends SplitShaftTileEntity {

	public GearshiftTileEntity(BlockEntityType<? extends GearshiftTileEntity> type) {
		super(type);
	}

	@Override
	public float getRotationSpeedModifier(Direction face) {
		if (hasSource()) {
			if (face != getSourceFacing() && getBlockState().getValue(BlockStateProperties.POWERED))
				return -1;
		}
		return 1;
	}
	
}
