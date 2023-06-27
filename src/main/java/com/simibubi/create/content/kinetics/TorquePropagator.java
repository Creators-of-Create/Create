package com.simibubi.create.content.kinetics;

import java.util.HashMap;
import java.util.Map;

import com.simibubi.create.Create;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
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

	public KineticNetwork getOrCreateNetworkFor(KineticBlockEntity be) {
		Long id = be.network;
		KineticNetwork network;
		Map<Long, KineticNetwork> map = networks.computeIfAbsent(be.getLevel(), $ -> new HashMap<>());
		if (id == null)
			return null;

		if (!map.containsKey(id)) {
			network = new KineticNetwork();
			network.id = be.network;
			map.put(id, network);
		}
		network = map.get(id);
		return network;
	}

}
