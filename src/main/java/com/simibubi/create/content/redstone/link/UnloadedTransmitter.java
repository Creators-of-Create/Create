package com.simibubi.create.content.redstone.link;

import net.createmod.catnip.data.Couple;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;

public class UnloadedTransmitter implements IRedstoneLinkable {

	private static final Couple<RedstoneLinkNetworkHandler.Frequency> dummy = Couple.create(() -> RedstoneLinkNetworkHandler.Frequency.EMPTY);
	private final int signal;
	private final BlockPos location;

	public CompoundTag save() {
		CompoundTag tag = new CompoundTag();
		tag.putInt("Signal", this.signal);
		tag.put("Location", NbtUtils.writeBlockPos(location));
		return tag;
	}

	public UnloadedTransmitter(final CompoundTag nbt) {
		this.signal = nbt.getInt("Signal");
		this.location = NbtUtils.readBlockPos(nbt, "Location").orElse(BlockPos.ZERO);
	}

	public UnloadedTransmitter(final IRedstoneLinkable linkable) {
		this.signal = linkable.getTransmittedStrength();
		this.location = linkable.getLocation();
	}

	@Override
	public int getTransmittedStrength() {
		return this.signal;
	}

	@Override
	public void setReceivedStrength(final int power) {

	}

	@Override
	public int getReceivedStrength() {
		return 0;
	}

	@Override
	public boolean isListening() {
		return false;
	}

	@Override
	public boolean isAlive() {
		return true;
	}

	@Override
	public Couple<RedstoneLinkNetworkHandler.Frequency> getNetworkKey() {
		return dummy;
	}

	@Override
	public BlockPos getLocation() {
		return this.location;
	}
}
