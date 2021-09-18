package com.simibubi.create.content.contraptions.relays.gearbox;

import com.simibubi.create.content.contraptions.relays.encased.DirectionalShaftHalvesTileEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class GearboxTileEntity extends DirectionalShaftHalvesTileEntity {

	public GearboxTileEntity(BlockPos pos, BlockState state, BlockEntityType<? extends GearboxTileEntity> type) {
		super(pos, state, type);
	}

	@Override
	protected boolean isNoisy() {
		return false;
	}

}
