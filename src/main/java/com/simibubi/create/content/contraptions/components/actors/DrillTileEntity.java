package com.simibubi.create.content.contraptions.components.actors;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class DrillTileEntity extends BlockBreakingKineticTileEntity {

	public DrillTileEntity(BlockPos pos, BlockState state, BlockEntityType<? extends DrillTileEntity> type) {
		super(pos, state, type);
	}

	@Override
	protected BlockPos getBreakingPos() {
		return getBlockPos().relative(getBlockState().getValue(DrillBlock.FACING));
	}

}
