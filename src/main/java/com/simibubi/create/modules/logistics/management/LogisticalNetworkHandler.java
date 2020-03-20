package com.simibubi.create.modules.logistics.management;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.simibubi.create.Create;
import com.simibubi.create.modules.logistics.management.base.LogisticalActorTileEntity;

import net.minecraft.world.IWorld;

public class LogisticalNetworkHandler {

	static Map<IWorld, Map<UUID, LogisticalNetwork>> networks = new HashMap<>();

	public void onLoadWorld(IWorld world) {
		networks.put(world, new HashMap<>());
		Create.logger.debug("Prepared Logistical Network Map for " + world.getDimension().getType().getRegistryName());
	}

	public void onUnloadWorld(IWorld world) {
		networks.remove(world);
		Create.logger.debug("Removed Logistical Network Map for " + world.getDimension().getType().getRegistryName());
	}

	public LogisticalNetwork handleAdded(LogisticalActorTileEntity te) {
		LogisticalNetwork networkByID = getNetworkByID(te.getWorld(), te.getNetworkId());
		if (te.address == null || te.address.isEmpty()) {
			te.address = networkByID.getNextAvailableAddress(te);
			te.sendData();
		}
		networkByID.addController(te);
		return networkByID;
	}

	public void handleRemoved(LogisticalActorTileEntity te) {
		getNetworkByID(te.getWorld(), te.getNetworkId()).removeController(te);
		removeIfEmpty(te.getWorld(), te.getNetworkId());
	}

	public LogisticalNetwork getNetworkByID(IWorld world, UUID id) {
		if (!networks.containsKey(world))
			networks.put(world, new HashMap<>());
		Map<UUID, LogisticalNetwork> worldNets = networks.get(world);
		if (!worldNets.containsKey(id))
			worldNets.put(id, new LogisticalNetwork());
		return worldNets.get(id);
	}

	private void removeIfEmpty(IWorld world, UUID id) {
		Map<UUID, LogisticalNetwork> worldNets = networks.get(world);
		if (worldNets.get(id).isEmpty())
			worldNets.remove(id);
	}

}
