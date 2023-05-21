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
import com.simibubi.create.foundation.networking.SimplePacketBase;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;

public class TrackGraphRollCallPacket extends SimplePacketBase {

	int[] ints;

	public TrackGraphRollCallPacket() {
		GlobalRailwayManager manager = Create.RAILWAYS;
		ints = new int[manager.trackNetworks.size() * 2];
		int i = 0;
		for (TrackGraph trackGraph : manager.trackNetworks.values()) {
			ints[i] = trackGraph.netId;
			ints[i + 1] = trackGraph.getChecksum();
			i += 2;
		}
	}

	public TrackGraphRollCallPacket(FriendlyByteBuf buffer) {
		ints = new int[buffer.readVarInt()];
		for (int i = 0; i < ints.length; i++)
			ints[i] = buffer.readInt();
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeVarInt(ints.length);
		for (int i : ints)
			buffer.writeInt(i);
	}

	@Override
	public boolean handle(Context context) {
		context.enqueueWork(() -> {
			GlobalRailwayManager manager = Create.RAILWAYS.sided(null);
			Set<UUID> unusedIds = new HashSet<>(manager.trackNetworks.keySet());
			List<Integer> failedIds = new ArrayList<>();
			Map<Integer, UUID> idByNetId = new HashMap<>();
			manager.trackNetworks.forEach((uuid, g) -> idByNetId.put(g.netId, uuid));

			for (int i = 0; i < ints.length; i += 2) {
				UUID uuid = idByNetId.get(ints[i]);
				if (uuid == null) {
					failedIds.add(ints[i]);
					continue;
				}
				unusedIds.remove(uuid);
				TrackGraph trackGraph = manager.trackNetworks.get(uuid);
				if (trackGraph.getChecksum() == ints[i + 1])
					continue;
				Create.LOGGER.warn("Track network: " + uuid.toString()
					.substring(0, 6) + " failed its checksum; Requesting refresh");
				failedIds.add(ints[i]);
			}

			for (Integer failed : failedIds)
				AllPackets.getChannel().sendToServer(new TrackGraphRequestPacket(failed));
			for (UUID unused : unusedIds)
				manager.trackNetworks.remove(unused);
		});
		return true;
	}

}
