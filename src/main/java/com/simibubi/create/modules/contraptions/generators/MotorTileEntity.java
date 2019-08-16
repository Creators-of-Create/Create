package com.simibubi.create.modules.contraptions.generators;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.modules.contraptions.base.KineticTileEntity;

public class MotorTileEntity extends KineticTileEntity {

	public MotorTileEntity() {
		super(AllTileEntities.MOTOR.type);
		setSpeed(50);
		setForce(10);
	}
	
	@Override
	public boolean hasFastRenderer() {
		return true;
	}
	
	@Override
	public boolean isSource() {
		return true;
	}

}
