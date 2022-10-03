package com.simibubi.create.compat.computercraft;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.simibubi.create.content.contraptions.relays.gauge.SpeedGaugeTileEntity;

import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IPeripheral;

public class SpeedGaugePeripheral implements IPeripheral {

	private final SpeedGaugeTileEntity tile;

	public SpeedGaugePeripheral(SpeedGaugeTileEntity tile) {
		this.tile = tile;
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

	@Override
	public boolean equals(@Nullable IPeripheral other) {
		return this == other;
	}

}
