package com.simibubi.create.modules.contraptions;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.simibubi.create.modules.contraptions.base.KineticTileEntity;

public class KineticNetwork {

	public UUID id;
	public boolean initialized;

	private float maxStress;
	private float currentStress;

	private float unloadedStressCapacity;
	private float unloadedStress;
	public Map<KineticTileEntity, Float> sources;
	public Map<KineticTileEntity, Float> members;

	public KineticNetwork() {
		id = UUID.randomUUID();
		sources = new HashMap<>();
		members = new HashMap<>();
	}

	public void initFromTE(KineticTileEntity te) {
		unloadedStressCapacity = te.maxStress;
		unloadedStress = te.currentStress;
		initialized = true;
		updateStress();
		updateStressCapacity();
	}

	public void addSilently(KineticTileEntity te) {
		if (members.containsKey(te))
			return;
		if (te.isSource()) {
			float capacity = te.getAddedStressCapacity();
			unloadedStressCapacity -= capacity;
			sources.put(te, capacity);
		}
		float stressApplied = te.getStressApplied();
		unloadedStress -= stressApplied;
		members.put(te, stressApplied);
	}

	public void add(KineticTileEntity te) {
		if (members.containsKey(te))
			return;

//		Debug.debugChatAndShowStack(te.getType().getRegistryName().getPath() + " added to Network", 5);

		if (te.isSource()) {
			sources.put(te, te.getAddedStressCapacity());
			updateStressCapacity();
		}

		members.put(te, te.getStressApplied());
		updateStress();
		sync();
	}

	public void updateCapacityFor(KineticTileEntity te, float capacity) {
		sources.put(te, capacity);
		updateStressCapacity();
	}

	public void updateStressFor(KineticTileEntity te, float stress) {
		members.put(te, stress);
		updateStress();
	}

	public void remove(KineticTileEntity te) {
		if (!members.containsKey(te))
			return;

//		Debug.debugChat(te.getType().getRegistryName().getPath() + " removed from Network");

		if (te.isSource()) {
			sources.remove(te);
			updateStressCapacity();
		}

		members.remove(te);
		updateStress();
		sync();
	}

	public void sync() {
		for (KineticTileEntity te : members.keySet())
			te.sync(maxStress, currentStress);
	}

	public void updateStressCapacity() {
		float presentCapacity = 0;
		for (KineticTileEntity te : sources.keySet())
			presentCapacity += sources.get(te) * getStressMultiplierForSpeed(te.getGeneratedSpeed());
		float newMaxStress = presentCapacity + unloadedStressCapacity;
		if (maxStress != newMaxStress) {
			maxStress = newMaxStress;
			sync();
//			Debug.debugChatAndShowStack("Current Stress level: " + currentStress + "/" + maxStress, 5);
		}

	}

	public void updateStress() {
		float presentStress = 0;

		for (KineticTileEntity te : members.keySet())
			presentStress += members.get(te) * getStressMultiplierForSpeed(te.speed);
		float newStress = presentStress + unloadedStress;
		if (currentStress != newStress) {
			currentStress = newStress;
			sync();
//			Debug.debugChatAndShowStack("Current Stress level: " + currentStress + "/" + maxStress, 5);
		}
	}

	private float getStressMultiplierForSpeed(float speed) {
		return Math.abs(speed);
	}

}
