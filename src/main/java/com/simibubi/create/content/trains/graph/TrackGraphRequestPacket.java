package com.simibubi.create.content.trains.graph;

import com.simibubi.create.AllPackets;
import com.simibubi.create.Create;
import net.createmod.catnip.net.base.ServerboundPacketPayload;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;

public record TrackGraphRequestPacket(int netId) implements ServerboundPacketPayload {
	public static final StreamCodec<ByteBuf, TrackGraphRequestPacket> STREAM_CODEC = ByteBufCodecs.INT.map(
			TrackGraphRequestPacket::new, TrackGraphRequestPacket::netId
	);

	@Override
	public void handle(ServerPlayer player) {
		for (TrackGraph trackGraph : Create.RAILWAYS.trackNetworks.values()) {
			if (trackGraph.netId == netId) {
				Create.RAILWAYS.sync.sendFullGraphTo(trackGraph, player);
				break;
			}
		}
	}

	@Override
	public PacketTypeProvider getTypeProvider() {
		return AllPackets.TRACK_GRAPH_REQUEST;
	}
}
