package com.simibubi.create.content.contraptions.components.actors;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class DrillBlockEntity extends BlockBreakingKineticBlockEntity {

	public DrillBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	protected BlockPos getBreakingPos() {
		return getBlockPos().relative(getBlockState().getValue(DrillBlock.FACING));
	}

}
