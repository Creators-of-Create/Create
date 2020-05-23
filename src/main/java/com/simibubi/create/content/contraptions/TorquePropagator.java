package com.simibubi.create.content.contraptions;

import java.util.HashMap;
import java.util.Map;

import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;

import net.minecraft.world.IWorld;

public class TorquePropagator {

	static Map<IWorld, Map<Long, KineticNetwork>> networks = new HashMap<>();

	public void onLoadWorld(IWorld world) {
		networks.put(world, new HashMap<>());
		Create.logger.debug("Prepared Kinetic Network Space for " + world.getDimension().getType().getRegistryName());
	}

	public void onUnloadWorld(IWorld world) {
		networks.remove(world);
		Create.logger.debug("Removed Kinetic Network Space for " + world.getDimension().getType().getRegistryName());
	}

	public KineticNetwork getOrCreateNetworkFor(KineticTileEntity te) {
		Long id = te.network;
		KineticNetwork network;
		Map<Long, KineticNetwork> map = networks.get(te.getWorld());
		if (id == null)
			return null;

		if (!map.containsKey(id)) {
			network = new KineticNetwork();
			network.id = te.network;
			map.put(id, network);
		}
		network = map.get(id);
		return network;
	}

}
