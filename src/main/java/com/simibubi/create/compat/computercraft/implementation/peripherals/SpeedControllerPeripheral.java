package com.simibubi.create.compat.computercraft.implementation.peripherals;

import org.jetbrains.annotations.NotNull;

import com.simibubi.create.content.contraptions.relays.advanced.SpeedControllerTileEntity;
import com.simibubi.create.foundation.tileEntity.behaviour.scrollvalue.ScrollValueBehaviour;

import dan200.computercraft.api.lua.LuaFunction;

public class SpeedControllerPeripheral extends SyncedPeripheral<SpeedControllerTileEntity> {

	private final ScrollValueBehaviour targetSpeed;

	public SpeedControllerPeripheral(SpeedControllerTileEntity tile, ScrollValueBehaviour targetSpeed) {
		super(tile);
		this.targetSpeed = targetSpeed;
	}

	@LuaFunction(mainThread = true)
	public final void setTargetSpeed(int speed) {
		this.targetSpeed.setValue(speed);
	}

	@LuaFunction
	public final float getTargetSpeed() {
		return this.targetSpeed.getValue();
	}

	@NotNull
	@Override
	public String getType() {
		return "Create_RotationSpeedController";
	}

}
