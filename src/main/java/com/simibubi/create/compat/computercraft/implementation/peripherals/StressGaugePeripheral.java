package com.simibubi.create.compat.computercraft.implementation.peripherals;

import org.jetbrains.annotations.NotNull;

import com.simibubi.create.content.contraptions.relays.gauge.StressGaugeTileEntity;

import dan200.computercraft.api.lua.LuaFunction;

public class StressGaugePeripheral extends SyncedPeripheral<StressGaugeTileEntity> {

	public StressGaugePeripheral(StressGaugeTileEntity tile) {
		super(tile);
	}

	@LuaFunction
	public final float getStress() {
		return this.tile.getNetworkStress();
	}

	@LuaFunction
	public final float getStressCapacity() {
		return this.tile.getNetworkCapacity();
	}

	@NotNull
	@Override
	public String getType() {
		return "Create_Stressometer";
	}

}
