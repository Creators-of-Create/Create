package com.simibubi.create.compat.computercraft.peripherals;

import com.simibubi.create.compat.computercraft.peripherals.PeripheralBase;

import org.jetbrains.annotations.NotNull;

import com.simibubi.create.content.contraptions.relays.gauge.StressGaugeTileEntity;

import dan200.computercraft.api.lua.LuaFunction;

public class StressGaugePeripheral extends PeripheralBase<StressGaugeTileEntity> {

	public StressGaugePeripheral(StressGaugeTileEntity tile) {
		super(tile);
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

}
