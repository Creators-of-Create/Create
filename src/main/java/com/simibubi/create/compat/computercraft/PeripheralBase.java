package com.simibubi.create.compat.computercraft;

import org.jetbrains.annotations.Nullable;

import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.world.level.block.entity.BlockEntity;

public abstract class PeripheralBase<T extends BlockEntity & ComputerControllable> implements IPeripheral {

	protected final T tile;

	public PeripheralBase(T tile) {
		this.tile = tile;
	}

	@Override
	public boolean equals(@Nullable IPeripheral other) {
		return this == other;
	}

}
