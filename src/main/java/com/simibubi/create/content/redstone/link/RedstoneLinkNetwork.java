package com.simibubi.create.content.redstone.link;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.createmod.catnip.nbt.NBTHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

public class RedstoneLinkNetwork {

	private final Set<IRedstoneLinkable> transmitters = new HashSet<>();
	private final Map<BlockPos, UnloadedTransmitter> unloadedTransmitters = new HashMap<>();

	private final Set<IRedstoneLinkable> receivers = new HashSet<>();

	public CompoundTag save() {
		CompoundTag nbt = new CompoundTag();
		nbt.put("data", NBTHelper.writeCompoundList(unloadedTransmitters.values(), UnloadedTransmitter::save));
		return nbt;
	}

	public RedstoneLinkNetwork(CompoundTag tag) {
		NBTHelper.iterateCompoundList(tag.getList("data", Tag.TAG_COMPOUND), compoundTag -> {
			UnloadedTransmitter transmitter = new UnloadedTransmitter(compoundTag);
			unloadedTransmitters.put(transmitter.getLocation(), transmitter);
		});
	}

	public RedstoneLinkNetwork() {

	}

	public Set<IRedstoneLinkable> getTransmitters() {
		if (unloadedTransmitters.isEmpty()) {
			return transmitters;
		} else {
			Set<IRedstoneLinkable> result = new HashSet<>(transmitters);
			result.addAll(unloadedTransmitters.values());
			return result;
		}
	}

	public Set<IRedstoneLinkable> getReceivers() {
		return receivers;
	}

	public void addTransmitter(final IRedstoneLinkable actor) {
		unloadedTransmitters.remove(actor.getLocation());
		transmitters.add(actor);
	}

	public void addReceiver(final IRedstoneLinkable actor) {
		unloadedTransmitters.remove(actor.getLocation());
		receivers.add(actor);
	}

	public void markUnloaded(final IRedstoneLinkable actor) {
		unloadedTransmitters.put(actor.getLocation(), new UnloadedTransmitter(actor));
		transmitters.remove(actor);
	}
}
