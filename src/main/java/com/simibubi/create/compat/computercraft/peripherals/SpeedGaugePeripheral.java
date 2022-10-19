package com.simibubi.create.compat.computercraft.peripherals;

import org.jetbrains.annotations.NotNull;

import com.simibubi.create.content.contraptions.relays.gauge.SpeedGaugeTileEntity;

import dan200.computercraft.api.lua.LuaFunction;

public class SpeedGaugePeripheral extends PeripheralBase<SpeedGaugeTileEntity> {

	public SpeedGaugePeripheral(SpeedGaugeTileEntity tile) {
		super(tile);
	}

	@LuaFunction
	public float getSpeed() {
		return this.tile.getSpeed();
	}

	@NotNull
	@Override
	public String getType() {
		return "Create_Speedometer";
	}

}
