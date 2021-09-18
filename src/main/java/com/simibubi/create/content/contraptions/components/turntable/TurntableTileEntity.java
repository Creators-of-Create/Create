package com.simibubi.create.content.contraptions.components.turntable;

import com.simibubi.create.content.contraptions.base.KineticTileEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class TurntableTileEntity extends KineticTileEntity {

	public TurntableTileEntity(BlockPos pos, BlockState state, BlockEntityType<? extends TurntableTileEntity> type) {
		super(type, pos, state);
	}

}
