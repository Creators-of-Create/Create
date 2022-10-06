package com.simibubi.create.compat.computercraft;

import java.util.concurrent.atomic.AtomicInteger;

import org.jetbrains.annotations.NotNull;

import com.simibubi.create.foundation.networking.AllPackets;

import dan200.computercraft.api.peripheral.IComputerAccess;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.PacketDistributor;

public abstract class SyncedPeripheralBase<T extends BlockEntity & SyncedComputerControllable> extends PeripheralBase<T> {

	private final AtomicInteger computers = new AtomicInteger();

	public SyncedPeripheralBase(T tile) {
		super(tile);
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

		tile.setHasAttachedComputer(hasAttachedComputer);
		AllPackets.channel.send(PacketDistributor.ALL.noArg(), new AttachedComputerPacket(tile.getBlockPos(), hasAttachedComputer));
	}

}
