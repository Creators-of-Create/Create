package com.simibubi.create.content.contraptions.components.waterwheel;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class LargeWaterWheelTileEntity extends WaterWheelTileEntity {

	public LargeWaterWheelTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	protected int getSize() {
		return 2;
	}

}
