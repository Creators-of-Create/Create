package com.simibubi.create.content.logistics.packagerLink;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.GlobalPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

public class GlobalLogisticsManager {

	public Map<UUID, LogisticsNetwork> logisticsNetworks;

	private LogisticsNetworkSavedData savedData;

	public GlobalLogisticsManager() {
		logisticsNetworks = new HashMap<>();
	}

	public void levelLoaded(LevelAccessor level) {
		MinecraftServer server = level.getServer();
		if (server == null || server.overworld() != level)
			return;
		logisticsNetworks = new HashMap<>();
		savedData = null;
		loadLogisticsData(server);
	}

	public boolean mayInteract(UUID networkId, Player player) {
		LogisticsNetwork network = logisticsNetworks.get(networkId);
		return network == null || network.owner == null || !network.locked || network.owner.equals(player.getUUID());
	}

	public boolean mayAdministrate(UUID networkId, Player player) {
		LogisticsNetwork network = logisticsNetworks.get(networkId);
		return network == null || network.owner == null || network.owner.equals(player.getUUID());
	}

	public boolean isLockable(UUID networkId) {
		LogisticsNetwork network = logisticsNetworks.get(networkId);
		return network != null;
	}

	public boolean isLocked(UUID networkId) {
		LogisticsNetwork network = logisticsNetworks.get(networkId);
		return network != null && network.locked;
	}

	public void linkAdded(UUID networkId, GlobalPos pos, UUID ownedBy) {
		LogisticsNetwork network = logisticsNetworks.computeIfAbsent(networkId, $ -> new LogisticsNetwork(networkId));
		network.totalLinks.add(pos);
		if (ownedBy != null && network.owner == null)
			network.owner = ownedBy;
		markDirty();
	}

	public void linkLoaded(UUID networkId, GlobalPos pos) {
		logisticsNetworks.computeIfAbsent(networkId, $ -> new LogisticsNetwork(networkId)).loadedLinks.add(pos);
	}

	public void linkRemoved(UUID networkId, GlobalPos pos) {
		LogisticsNetwork logisticsNetwork = logisticsNetworks.get(networkId);
		if (logisticsNetwork == null)
			return;
		logisticsNetwork.totalLinks.remove(pos);
		logisticsNetwork.loadedLinks.remove(pos);
		if (logisticsNetwork.totalLinks.size() <= 0)
			logisticsNetworks.remove(networkId);
		markDirty();
	}

	public void linkInvalidated(UUID networkId, GlobalPos pos) {
		LogisticsNetwork logisticsNetwork = logisticsNetworks.get(networkId);
		if (logisticsNetwork == null)
			return;
		logisticsNetwork.loadedLinks.remove(pos);
	}

	public int getUnloadedLinkCount(UUID networkId) {
		LogisticsNetwork logisticsNetwork = logisticsNetworks.get(networkId);
		if (logisticsNetwork == null)
			return 0;
		return logisticsNetwork.totalLinks.size() - logisticsNetwork.loadedLinks.size();
	}

	@Nullable
	public RequestPromiseQueue getQueuedPromises(UUID networkId) {
		return !logisticsNetworks.containsKey(networkId) ? null : logisticsNetworks.get(networkId).panelPromises;
	}

	public boolean hasQueuedPromises(UUID networkId) {
		return logisticsNetworks.containsKey(networkId) && !logisticsNetworks.get(networkId).panelPromises.isEmpty();
	}

	private void loadLogisticsData(MinecraftServer server) {
		if (savedData != null)
			return;
		savedData = LogisticsNetworkSavedData.load(server);
		logisticsNetworks = savedData.getLogisticsNetworks();
	}

	public void tick(Level level) {
		if (level.dimension() != Level.OVERWORLD)
			return;
		logisticsNetworks.forEach((id, network) -> {
			network.panelPromises.tick();
		});
	}

	public void markDirty() {
		if (savedData != null)
			savedData.setDirty();
	}

}
