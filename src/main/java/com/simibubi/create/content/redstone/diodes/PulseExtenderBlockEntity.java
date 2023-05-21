package com.simibubi.create.content.redstone.diodes;

import static com.simibubi.create.content.redstone.diodes.BrassDiodeBlock.POWERING;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class PulseExtenderBlockEntity extends BrassDiodeBlockEntity {

	public PulseExtenderBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	protected void updateState(boolean powered, boolean powering, boolean atMax, boolean atMin) {
		if (atMin && !powered)
			return;
		if (atMin || powered) {
			level.setBlockAndUpdate(worldPosition, getBlockState().setValue(POWERING, true));
			state = maxState.getValue();
			return;
		}
		
		if (state == 1) {
			if (powering && !level.isClientSide)
				level.setBlockAndUpdate(worldPosition, getBlockState().setValue(POWERING, false));
			if (!powered)
				state = 0;
			return;
		}
		
		if (!powered)
			state--;
	}
}
