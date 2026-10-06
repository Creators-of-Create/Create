package com.simibubi.create.content.redstone.link;

import java.util.HashSet;
import java.util.Set;

public class RedstoneLinkNetwork {

	private final Set<IRedstoneLinkable> transmitters = new HashSet<>();
	private final Set<IRedstoneLinkable> receivers = new HashSet<>();

	public Set<IRedstoneLinkable> getTransmitters() {
		return transmitters;
	}

	public Set<IRedstoneLinkable> getReceivers() {
		return receivers;
	}
}
