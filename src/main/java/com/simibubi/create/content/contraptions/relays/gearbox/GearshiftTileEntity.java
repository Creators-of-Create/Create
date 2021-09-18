package com.simibubi.create.content.contraptions.relays.gearbox;

import com.simibubi.create.content.contraptions.relays.encased.SplitShaftTileEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.Direction;

public class GearshiftTileEntity extends SplitShaftTileEntity {

	public GearshiftTileEntity(BlockPos pos, BlockState state, BlockEntityType<? extends GearshiftTileEntity> type) {
		super(type, pos, state);
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
