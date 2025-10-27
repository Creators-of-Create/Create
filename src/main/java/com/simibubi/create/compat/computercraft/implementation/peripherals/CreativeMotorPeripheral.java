package com.simibubi.create.compat.computercraft.implementation.peripherals;

import org.jetbrains.annotations.NotNull;

import com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;

import dan200.computercraft.api.lua.LuaFunction;

public class CreativeMotorPeripheral extends SyncedPeripheral<CreativeMotorBlockEntity> {

	private final ScrollValueBehaviour generatedSpeed;

	public CreativeMotorPeripheral(CreativeMotorBlockEntity blockEntity, ScrollValueBehaviour generatedSpeed) {
		super(blockEntity);
		this.generatedSpeed = generatedSpeed;
	}

	@LuaFunction(mainThread = true)
	public final void setGeneratedSpeed(int speed) {
		this.generatedSpeed.setValue(speed);
	}

	@LuaFunction
	public final float getGeneratedSpeed() {
		return this.generatedSpeed.getValue();
	}

	@NotNull
	@Override
	public String getType() {
		return "Create_CreativeMotor";
	}

}
