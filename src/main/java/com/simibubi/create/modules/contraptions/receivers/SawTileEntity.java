package com.simibubi.create.modules.contraptions.receivers;

import static com.simibubi.create.modules.contraptions.receivers.SawBlock.RUNNING;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.modules.contraptions.base.KineticTileEntity;

public class SawTileEntity extends KineticTileEntity {

	public SawTileEntity() {
		super(AllTileEntities.SAW.type);
	}

	@Override
	public void onSpeedChanged() {
		boolean shouldRun = Math.abs(speed) > 1 / 64f;
		boolean running = getBlockState().get(RUNNING);
		if (shouldRun != running)
			world.setBlockState(pos, getBlockState().with(RUNNING, shouldRun));
	}

}
