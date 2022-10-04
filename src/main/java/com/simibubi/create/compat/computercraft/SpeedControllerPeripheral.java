package com.simibubi.create.compat.computercraft;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.simibubi.create.foundation.tileEntity.behaviour.scrollvalue.ScrollValueBehaviour;

import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IPeripheral;

public class SpeedControllerPeripheral implements IPeripheral {

	private final ScrollValueBehaviour targetSpeed;

	public SpeedControllerPeripheral(ScrollValueBehaviour targetSpeed) {
		this.targetSpeed = targetSpeed;
	}

	@LuaFunction(mainThread = true)
	public void setTargetSpeed(int speed) {
		this.targetSpeed.setValue(speed);
	}

	@LuaFunction
	public float getTargetSpeed() {
		return this.targetSpeed.getValue();
	}

	@NotNull
	@Override
	public String getType() {
		return "Create_RotationSpeedController";
	}

	@Override
	public boolean equals(@Nullable IPeripheral other) {
		return this == other;
	}

}
