package com.simibubi.create.content.logistics.block.diodes;

import static com.simibubi.create.content.logistics.block.diodes.AdjustableRepeaterBlock.POWERING;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class AdjustablePulseRepeaterTileEntity extends AdjustableRepeaterTileEntity {

	public AdjustablePulseRepeaterTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	protected void updateState(boolean powered, boolean powering, boolean atMax, boolean atMin) {
		if (!charging && powered && !atMax)
			charging = true;

		if (charging && atMax) {
			if (powering) {
				level.setBlockAndUpdate(worldPosition, getBlockState().setValue(POWERING, false));
				charging = false;
				return;
			}
			if (!powering && !level.isClientSide)
				level.setBlockAndUpdate(worldPosition, getBlockState().setValue(POWERING, true));
			return;
		}
		
		if (!charging && powered)
			return;

		if (!charging && !atMin) {
			if (!level.isClientSide)
				level.setBlockAndUpdate(worldPosition, getBlockState().setValue(POWERING, false));
			state = 0;
			return;
		}

		state += charging ? 1 : 0;
	}
	
}
