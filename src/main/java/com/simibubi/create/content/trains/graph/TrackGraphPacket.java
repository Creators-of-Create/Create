package com.simibubi.create.content.trains.graph;

import java.util.UUID;

import com.simibubi.create.CreateClient;
import com.simibubi.create.content.trains.GlobalRailwayManager;
import net.createmod.catnip.net.base.ClientboundPacketPayload;

import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;

public abstract class TrackGraphPacket implements ClientboundPacketPayload {

	public UUID graphId;
	public int netId;
	public boolean packetDeletesGraph;

	@Override
	public void handle(Player player) {
		this.handle(CreateClient.RAILWAYS, CreateClient.RAILWAYS.getOrCreateGraph(graphId, netId));
	}

	protected abstract void handle(GlobalRailwayManager manager, TrackGraph graph);

}
