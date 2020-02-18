package com.simibubi.create.modules.logistics.block.diodes;

import static com.simibubi.create.modules.logistics.block.diodes.FlexpeaterBlock.POWERING;

import com.simibubi.create.AllTileEntities;

public class FlexPulsepeaterTileEntity extends FlexpeaterTileEntity {

	public FlexPulsepeaterTileEntity() {
		super(AllTileEntities.FLEXPULSEPEATER.type);
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
