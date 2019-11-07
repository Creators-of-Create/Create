package com.simibubi.create.modules.contraptions;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.simibubi.create.Create;
import com.simibubi.create.modules.contraptions.base.KineticTileEntity;

import net.minecraft.world.IWorld;

public class TorquePropagator {

	static Map<IWorld, Map<UUID, KineticNetwork>> networks = new HashMap<>();

	public void onLoadWorld(IWorld world) {
		networks.put(world, new HashMap<>());
		Create.logger.debug("Prepared Kinetic Network Space for " + world.getDimension().getType().getRegistryName());
	}

	public void onUnloadWorld(IWorld world) {
		networks.remove(world);
		Create.logger.debug("Removed Kinetic Network Space for " + world.getDimension().getType().getRegistryName());
	}

	public KineticNetwork getNetworkFor(KineticTileEntity te) {
		UUID id = te.getNetworkID();
		KineticNetwork network;
		Map<UUID, KineticNetwork> map = networks.get(te.getWorld());
		if (id == null) {
			network = new KineticNetwork();

//			Debug.debugChatAndShowStack(te.getType().getRegistryName().getPath() + " created new Network", 5);

			te.newNetworkID = network.id;
			te.updateNetwork = true;
			map.put(id, network);
		} else {
			if (!map.containsKey(id)) {
				network = new KineticNetwork();
				network.id = te.getNetworkID();
				map.put(id, network);
			}
			network = map.get(id);
		}
		return network;
	}

}
