package com.simibubi.create.content.logistics.trains;

import java.util.UUID;
import java.util.function.Supplier;

import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.networking.SimplePacketBase;

import net.minecraftforge.network.NetworkEvent.Context;

public abstract class TrackGraphPacket extends SimplePacketBase {

	public UUID graphId;
	public int netId;
	public boolean packetDeletesGraph;

	@Override
	public void handle(Supplier<Context> context) {
		context.get()
			.enqueueWork(() -> handle(CreateClient.RAILWAYS, CreateClient.RAILWAYS.getOrCreateGraph(graphId, netId)));
		context.get()
			.setPacketHandled(true);
	}

	protected abstract void handle(GlobalRailwayManager manager, TrackGraph graph);

}
