package com.simibubi.create.compat.computercraft.implementation.peripherals;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.compat.computercraft.events.ComputerEvent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.simibubi.create.AllPackets;
import com.simibubi.create.compat.computercraft.AttachedComputerPacket;
import com.simibubi.create.compat.computercraft.implementation.ComputerBehaviour;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;

import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraftforge.network.PacketDistributor;

public abstract class SyncedPeripheral<T extends SmartBlockEntity> implements IPeripheral {

	protected final T blockEntity;
	private final List<@NotNull IComputerAccess> computers = new ArrayList<>();

	public SyncedPeripheral(T blockEntity) {
		this.blockEntity = blockEntity;
	}

	@Override
	public void attach(@NotNull IComputerAccess computer) {
		synchronized (computers) {
			computers.add(computer);
			if (computers.size() == 1)
				onFirstAttach();
			updateBlockEntity();
		}
	}

	protected void onFirstAttach() {}

	@Override
	public void detach(@NotNull IComputerAccess computer) {
		synchronized (computers) {
			computers.remove(computer);
			updateBlockEntity();
			if (computers.isEmpty())
				onLastDetach();
		}
	}

	protected void onLastDetach() {}

	private void updateBlockEntity() {
		boolean hasAttachedComputer = !computers.isEmpty();

		blockEntity.getBehaviour(ComputerBehaviour.TYPE).setHasAttachedComputer(hasAttachedComputer);
		AllPackets.getChannel().send(PacketDistributor.ALL.noArg(),
				new AttachedComputerPacket(blockEntity.getBlockPos(), hasAttachedComputer));
	}

	@Override
	public boolean equals(@Nullable IPeripheral other) {
		return this == other;
	}

}
