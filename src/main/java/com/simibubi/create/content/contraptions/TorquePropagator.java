package com.simibubi.create.content.contraptions;

import java.util.HashMap;
import java.util.Map;

import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.foundation.utility.WorldHelper;

import net.minecraft.world.level.LevelAccessor;

public class TorquePropagator {

	static Map<LevelAccessor, Map<Long, KineticNetwork>> networks = new HashMap<>();

	public void onLoadWorld(LevelAccessor world) {
		networks.put(world, new HashMap<>());
		Create.LOGGER.debug("Prepared Kinetic Network Space for " + WorldHelper.getDimensionID(world));
	}

	public void onUnloadWorld(LevelAccessor world) {
		networks.remove(world);
		Create.LOGGER.debug("Removed Kinetic Network Space for " + WorldHelper.getDimensionID(world));
	}

	public KineticNetwork getOrCreateNetworkFor(KineticTileEntity te) {
		Long id = te.network;
		KineticNetwork network;
		Map<Long, KineticNetwork> map = networks.computeIfAbsent(te.getLevel(), $ -> new HashMap<>());
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
