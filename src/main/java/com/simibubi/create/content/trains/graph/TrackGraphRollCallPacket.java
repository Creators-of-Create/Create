package com.simibubi.create.content.trains.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.simibubi.create.AllPackets;
import com.simibubi.create.Create;
import com.simibubi.create.content.trains.GlobalRailwayManager;

import io.netty.buffer.ByteBuf;
import net.createmod.catnip.codecs.stream.CatnipStreamCodecBuilders;
import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public record TrackGraphRollCallPacket(List<Entry> entries) implements ClientboundPacketPayload {
	public static final StreamCodec<ByteBuf, TrackGraphRollCallPacket> STREAM_CODEC = CatnipStreamCodecBuilders.list(Entry.STREAM_CODEC).map(
					TrackGraphRollCallPacket::new, TrackGraphRollCallPacket::entries
			);

	public static TrackGraphRollCallPacket ofServer() {
		List<Entry> entries = new ArrayList<>();
		for (TrackGraph graph : Create.RAILWAYS.trackNetworks.values()) {
			entries.add(new Entry(graph.netId, graph.getChecksum()));
		}
		return new TrackGraphRollCallPacket(entries);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void handle(LocalPlayer player) {
		GlobalRailwayManager manager = Create.RAILWAYS.sided(null);
		Set<UUID> unusedIds = new HashSet<>(manager.trackNetworks.keySet());
		List<Integer> failedIds = new ArrayList<>();
		Map<Integer, UUID> idByNetId = new HashMap<>();
		manager.trackNetworks.forEach((uuid, g) -> idByNetId.put(g.netId, uuid));

		for (Entry entry : this.entries) {
			UUID uuid = idByNetId.get(entry.netId);
			if (uuid == null) {
				failedIds.add(entry.netId);
				continue;
			}
			unusedIds.remove(uuid);
			TrackGraph trackGraph = manager.trackNetworks.get(uuid);
			if (trackGraph.getChecksum() == entry.checksum)
				continue;
			Create.LOGGER.warn("Track network: {} failed its checksum; Requesting refresh", uuid.toString().substring(0, 6));
			failedIds.add(entry.netId);
		}

		for (Integer failed : failedIds)
			CatnipServices.NETWORK.sendToServer(new TrackGraphRequestPacket(failed));
		for (UUID unused : unusedIds)
			manager.trackNetworks.remove(unused);
	}

	@Override
	public PacketTypeProvider getTypeProvider() {
		return AllPackets.TRACK_GRAPH_ROLL_CALL;
	}

	public record Entry(int netId, int checksum) {
		public static final StreamCodec<ByteBuf, Entry> STREAM_CODEC = StreamCodec.composite(
				ByteBufCodecs.VAR_INT, Entry::netId,
				ByteBufCodecs.INT, Entry::checksum,
				Entry::new
		);
	}
}
