package com.simibubi.create.content.contraptions.fluids;

import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.foundation.gui.widgets.InterpolatedChasingValue;

import net.minecraft.tileentity.TileEntityType;

public class PumpTileEntity extends KineticTileEntity {

	InterpolatedChasingValue arrowDirection;

	public PumpTileEntity(TileEntityType<?> typeIn) {
		super(typeIn);
		arrowDirection = new InterpolatedChasingValue();
		arrowDirection.start(1);
	}

	@Override
	public void tick() {
		super.tick();
		if (world.isRemote) {
			float speed = getSpeed();
			if (speed != 0)
				arrowDirection.target(Math.signum(speed));
			arrowDirection.tick();
		}
	}

}
