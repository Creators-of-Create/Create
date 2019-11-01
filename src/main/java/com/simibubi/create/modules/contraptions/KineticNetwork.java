package com.simibubi.create.modules.contraptions;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.modules.contraptions.base.KineticTileEntity;

public class KineticNetwork {

	public UUID id;
	private float stressCapacityPool;
	private float maxStress;
	private float currentStress;
	public boolean initialized;

	public Map<KineticTileEntity, Float> sources;
	public Set<KineticTileEntity> members;

	public KineticNetwork() {
		id = UUID.randomUUID();
		maxStress = stressCapacityPool = 0;
		setCurrentStress(0);
		sources = new HashMap<>();
		members = new HashSet<>();
	}
	
	public void initFromTE(KineticTileEntity te) {
		maxStress = stressCapacityPool = te.maxStress;
		currentStress = te.currentStress;
		initialized = true;
		addSilently(te);
	}
	
	public void addSilently(KineticTileEntity te) {
		if (members.contains(te))
			return;
		if (te.isSource()) {
			float capacity = te.getAddedStressCapacity();
			stressCapacityPool -= capacity;
			sources.put(te, capacity);
		}
		members.add(te);
	}

	public void add(KineticTileEntity te) {
		if (members.contains(te))
			return;

		Lang.debugChat(te.getType().getRegistryName().getPath() + " added to Network");

		te.setNetworkID(this.id);
		
		if (te.isSource()) {
			float capacity = te.getAddedStressCapacity();
			sources.put(te, capacity);
			updateMaxStress();
		}
		members.add(te);
		setCurrentStress(getCurrentStress() + te.getStressApplied());
		sync();
	}
	
	public void updateCapacityFor(KineticTileEntity te, float capacity) {
		sources.put(te, capacity);
		updateMaxStress();
	}

	public void remove(KineticTileEntity te) {
		if (!members.contains(te))
			return;

		Lang.debugChat(te.getType().getRegistryName().getPath() + " removed from Network");

		if (te.isSource()) {
			sources.remove(te);
			updateMaxStress();
		}
		members.remove(te);
		setCurrentStress(getCurrentStress() - te.getStressApplied());
		sync();
	}

	public void sync() {
		for (KineticTileEntity te : members) {
			te.sync(id, getMaxStress(), getCurrentStress());
		}
	}

	public float getMaxStress() {
		return maxStress;
	}

	private void updateMaxStress() {
		float presentCapacity = 0;
		for (Float cap : sources.values())
			presentCapacity += cap;
		float newMaxStress = presentCapacity + stressCapacityPool;
		if (maxStress != newMaxStress) {
			maxStress = newMaxStress;
			sync();
		}
		Lang.debugChat("Current Stress level: " + currentStress + "/" + maxStress);
	}

	public float getCurrentStress() {
		return currentStress;
	}

	public void setCurrentStress(float currentStress) {
		this.currentStress = currentStress;
		Lang.debugChat("Current Stress level: " + currentStress + "/" + maxStress);
	}

}
