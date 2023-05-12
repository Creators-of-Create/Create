package com.simibubi.create.compat.computercraft.implementation.peripherals;

import org.jetbrains.annotations.NotNull;

import com.simibubi.create.content.contraptions.relays.gauge.SpeedGaugeBlockEntity;

import dan200.computercraft.api.lua.LuaFunction;

public class SpeedGaugePeripheral extends SyncedPeripheral<SpeedGaugeBlockEntity> {

	public SpeedGaugePeripheral(SpeedGaugeBlockEntity tile) {
		super(tile);
	}

	@LuaFunction
	public final float getSpeed() {
		return this.tile.getSpeed();
	}

	@NotNull
	@Override
	public String getType() {
		return "Create_Speedometer";
	}

}
