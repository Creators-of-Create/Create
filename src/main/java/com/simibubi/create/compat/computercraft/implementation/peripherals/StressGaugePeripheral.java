package com.simibubi.create.compat.computercraft.implementation.peripherals;

import org.jetbrains.annotations.NotNull;

import com.simibubi.create.content.contraptions.relays.gauge.StressGaugeBlockEntity;

import dan200.computercraft.api.lua.LuaFunction;

public class StressGaugePeripheral extends SyncedPeripheral<StressGaugeBlockEntity> {

	public StressGaugePeripheral(StressGaugeBlockEntity tile) {
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
