package com.simibubi.create.content.logistics.block.diodes;

import static com.simibubi.create.content.logistics.block.diodes.AdjustableRepeaterBlock.POWERING;

import net.minecraft.tileentity.TileEntityType;

public class AdjustablePulseRepeaterTileEntity extends AdjustableRepeaterTileEntity {

	public AdjustablePulseRepeaterTileEntity(TileEntityType<? extends AdjustablePulseRepeaterTileEntity> type) {
		super(type);
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
