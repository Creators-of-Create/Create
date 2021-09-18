package com.simibubi.create.content.contraptions.relays.encased;

import com.simibubi.create.content.contraptions.base.KineticTileEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class EncasedShaftTileEntity extends KineticTileEntity {

	public EncasedShaftTileEntity(BlockPos pos, BlockState state, BlockEntityType<? extends EncasedShaftTileEntity> type) {
		super(type, pos, state);
	}

}
