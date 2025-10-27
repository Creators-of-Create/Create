package com.simibubi.create.content.kinetics.chainConveyor;

import java.util.Map;
import java.util.UUID;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.world.entity.player.Player;

public class ServerChainConveyorHandler {


	public static Object2IntMap<UUID> hangingPlayers = new Object2IntOpenHashMap<>();

	public static int ticks;

	public static void handleTTLPacket(Player player) {
		int count = hangingPlayers.size();
		hangingPlayers.put(player.getUUID(), 20);

		if (hangingPlayers.size() != count)
			sync();
	}
	
	public static void handleStopRidingPacket(Player player) {
		if (hangingPlayers.removeInt(player.getUUID()) != 0)
			sync();
	}

	public static void tick() {
		ticks++;

		int before = hangingPlayers.size();

		ObjectIterator<Object2IntMap.Entry<UUID>> iterator = hangingPlayers.object2IntEntrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<UUID, Integer> entry = iterator.next();
			int newTTL = entry.getValue() - 1;
			if (newTTL <= 0) {
				iterator.remove();
			} else {
				entry.setValue(newTTL);
			}
		}

		int after = hangingPlayers.size();

		if (ticks % 10 != 0 && before == after)
			return;

		sync();

	}

	public static void sync() {
		CatnipServices.NETWORK.sendToAllClients(new ClientboundChainConveyorRidingPacket(hangingPlayers.keySet()));
	}

}
