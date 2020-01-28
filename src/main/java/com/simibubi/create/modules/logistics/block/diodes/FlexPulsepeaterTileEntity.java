package com.simibubi.create.modules.logistics.block.diodes;

import static com.simibubi.create.modules.logistics.block.diodes.FlexpeaterBlock.POWERING;
import static net.minecraft.block.RedstoneDiodeBlock.POWERED;

import com.simibubi.create.AllTileEntities;

public class FlexPulsepeaterTileEntity extends FlexpeaterTileEntity {

	public FlexPulsepeaterTileEntity() {
		super(AllTileEntities.FLEXPULSEPEATER.type);
	}
	
	@Override
	public void tick() {
		updateConfigurableValue();
		
		boolean powered = getBlockState().get(POWERED);
		boolean powering = getBlockState().get(POWERING);
		boolean atMax = state >= maxState;
		boolean isReset = state == 0;

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

		if (!charging && !isReset) {
			if (!world.isRemote)
				world.setBlockState(pos, getBlockState().with(POWERING, false));
			state = 0;
			return;
		}

		state += charging ? 1 : 0;
	}
	
}
