package com.simibubi.create.compat.trainmap;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.simibubi.create.compat.trainmap.TrainMapSync.TrainMapSyncEntry;

import net.createmod.catnip.platform.CatnipServices;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.data.Pair;

public class TrainMapSyncClient {

	public static Map<UUID, TrainMapSyncEntry> currentData = new HashMap<>();

	public static double lastPacket;

	private static int ticks;

	public static void requestData() {
		ticks++;
		if (ticks % 5 == 0)
			CatnipServices.NETWORK.sendToServer(TrainMapSyncRequestPacket.INSTANCE);
	}

	public static void stopRequesting() {
		ticks = 0;
		currentData.clear();
	}

	public static void receive(TrainMapSyncPacket packet) {
		if (ticks == 0)
			return;

		lastPacket = AnimationTickHolder.getTicks();
		lastPacket += AnimationTickHolder.getPartialTicks();

		Set<UUID> staleEntries = new HashSet<>(currentData.keySet());

		for (Pair<UUID, TrainMapSyncEntry> pair : packet.entries) {
			UUID id = pair.getFirst();
			TrainMapSyncEntry entry = pair.getSecond();
			staleEntries.remove(id);
			currentData.computeIfAbsent(id, $ -> entry)
				.updateFrom(entry, packet.light);
		}

		for (UUID uuid : staleEntries)
			currentData.remove(uuid);
	}

}
