package com.simibubi.create.compat.computercraft.implementation.peripherals;

import java.util.concurrent.atomic.AtomicInteger;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.simibubi.create.compat.computercraft.AttachedComputerPacket;
import com.simibubi.create.compat.computercraft.implementation.ComputerBehaviour;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;

import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraftforge.network.PacketDistributor;

public abstract class SyncedPeripheral<T extends SmartTileEntity> implements IPeripheral {

	protected final T tile;
	private final AtomicInteger computers = new AtomicInteger();

	public SyncedPeripheral(T tile) {
		this.tile = tile;
	}

	@Override
	public void attach(@NotNull IComputerAccess computer) {
		computers.incrementAndGet();
		updateTile();
	}

	@Override
	public void detach(@NotNull IComputerAccess computer) {
		computers.decrementAndGet();
		updateTile();
	}

	private void updateTile() {
		boolean hasAttachedComputer = computers.get() > 0;

		tile.getBehaviour(ComputerBehaviour.TYPE).setHasAttachedComputer(hasAttachedComputer);
		AllPackets.channel.send(PacketDistributor.ALL.noArg(), new AttachedComputerPacket(tile.getBlockPos(), hasAttachedComputer));
	}

	@Override
	public boolean equals(@Nullable IPeripheral other) {
		return this == other;
	}

}
