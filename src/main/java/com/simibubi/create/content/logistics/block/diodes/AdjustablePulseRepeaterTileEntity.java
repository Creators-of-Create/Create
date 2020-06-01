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
				world.setBlockState(pos, getBlockState().with(POWERING, false));
				charging = false;
				return;
			}
			if (!powering && !world.isRemote)
				world.setBlockState(pos, getBlockState().with(POWERING, true));
			return;
		}
		
		if (!charging && powered)
			return;

		if (!charging && !atMin) {
			if (!world.isRemote)
				world.setBlockState(pos, getBlockState().with(POWERING, false));
			state = 0;
			return;
		}

		state += charging ? 1 : 0;
	}
	
}
