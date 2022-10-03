package com.simibubi.create.compat.computercraft;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.simibubi.create.content.contraptions.relays.gauge.StressGaugeTileEntity;

import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IPeripheral;

public class StressGaugePeripheral implements IPeripheral {

	private final StressGaugeTileEntity tile;

	public StressGaugePeripheral(StressGaugeTileEntity tile) {
		this.tile = tile;
	}

	@LuaFunction
	public float getStress() {
		return this.tile.getNetworkStress();
	}

	@LuaFunction
	public float getStressCapacity() {
		return this.tile.getNetworkCapacity();
	}

	@NotNull
	@Override
	public String getType() {
		return "Create_Stressometer";
	}

	@Override
	public boolean equals(@Nullable IPeripheral other) {
		return this == other;
	}

}
