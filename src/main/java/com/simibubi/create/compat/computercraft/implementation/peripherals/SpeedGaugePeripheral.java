package com.simibubi.create.compat.computercraft.implementation.peripherals;

import org.jetbrains.annotations.NotNull;

import com.simibubi.create.content.contraptions.relays.gauge.SpeedGaugeTileEntity;

import dan200.computercraft.api.lua.LuaFunction;

public class SpeedGaugePeripheral extends SyncedPeripheral<SpeedGaugeTileEntity> {

	public SpeedGaugePeripheral(SpeedGaugeTileEntity tile) {
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
