package com.simibubi.create.content.contraptions;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.simibubi.create.content.contraptions.base.KineticTileEntity;

public class KineticNetwork {

	public Long id;
	public boolean initialized;
	public Map<KineticTileEntity, Float> sources;
	public Map<KineticTileEntity, Float> members;

	private float currentCapacity;
	private float currentStress;
	private float unloadedCapacity;
	private float unloadedStress;
	private int unloadedMembers;

	public KineticNetwork() {
		sources = new HashMap<>();
		members = new HashMap<>();
	}

	public void initFromTE(float maxStress, float currentStress, int members) {
		unloadedCapacity = maxStress;
		unloadedStress = currentStress;
		unloadedMembers = members;
		initialized = true;
		updateStress();
		updateCapacity();
	}

	public void addSilently(KineticTileEntity te, float lastCapacity, float lastStress) {
		if (members.containsKey(te))
			return;
		if (te.isSource()) {
			unloadedCapacity -= lastCapacity * getStressMultiplierForSpeed(te.getGeneratedSpeed());
			float addedStressCapacity = te.calculateAddedStressCapacity();
			sources.put(te, addedStressCapacity);
		}

		unloadedStress -= lastStress * getStressMultiplierForSpeed(te.getTheoreticalSpeed());
		float stressApplied = te.calculateStressApplied();
		members.put(te, stressApplied);

		unloadedMembers--;
		if (unloadedMembers < 0)
			unloadedMembers = 0;
		if (unloadedCapacity < 0)
			unloadedCapacity = 0;
		if (unloadedStress < 0)
			unloadedStress = 0;
	}

	public void add(KineticTileEntity te) {
		if (members.containsKey(te))
			return;
		if (te.isSource())
			sources.put(te, te.calculateAddedStressCapacity());
		members.put(te, te.calculateStressApplied());
		updateFromNetwork(te);
		te.networkDirty = true;
	}

	public void updateCapacityFor(KineticTileEntity te, float capacity) {
		sources.put(te, capacity);
		updateCapacity();
	}

	public void updateStressFor(KineticTileEntity te, float stress) {
		members.put(te, stress);
		updateStress();
	}

	public void remove(KineticTileEntity te) {
		if (!members.containsKey(te))
			return;
		if (te.isSource())
			sources.remove(te);
		members.remove(te);
		te.updateFromNetwork(0, 0, 0);

		if (members.isEmpty()) {
			TorquePropagator.networks.get(te.getLevel())
				.remove(this.id);
			return;
		}

		members.keySet()
			.stream()
			.findFirst()
			.map(member -> member.networkDirty = true);
	}

	public void sync() {
		for (KineticTileEntity te : members.keySet())
			updateFromNetwork(te);
	}

	private void updateFromNetwork(KineticTileEntity te) {
		te.updateFromNetwork(currentCapacity, currentStress, getSize());
	}

	public void updateCapacity() {
		float newMaxStress = calculateCapacity();
		if (currentCapacity != newMaxStress) {
			currentCapacity = newMaxStress;
			sync();
		}
	}

	public void updateStress() {
		float newStress = calculateStress();
		if (currentStress != newStress) {
			currentStress = newStress;
			sync();
		}
	}

	public void updateNetwork() {
		float newStress = calculateStress();
		float newMaxStress = calculateCapacity();
		if (currentStress != newStress || currentCapacity != newMaxStress) {
			currentStress = newStress;
			currentCapacity = newMaxStress;
			sync();
		}
	}

	public float calculateCapacity() {
		float presentCapacity = 0;
		for (Iterator<KineticTileEntity> iterator = sources.keySet()
			.iterator(); iterator.hasNext();) {
			KineticTileEntity te = iterator.next();
			if (te.getLevel()
				.getBlockEntity(te.getBlockPos()) != te) {
				iterator.remove();
				continue;
			}
			presentCapacity += getActualCapacityOf(te);
		}
		float newMaxStress = presentCapacity + unloadedCapacity;
		return newMaxStress;
	}

	public float calculateStress() {
		float presentStress = 0;
		for (Iterator<KineticTileEntity> iterator = members.keySet()
			.iterator(); iterator.hasNext();) {
			KineticTileEntity te = iterator.next();
			if (te.getLevel()
				.getBlockEntity(te.getBlockPos()) != te) {
				iterator.remove();
				continue;
			}
			presentStress += getActualStressOf(te);
		}
		float newStress = presentStress + unloadedStress;
		return newStress;
	}

	public float getActualCapacityOf(KineticTileEntity te) {
		return sources.get(te) * getStressMultiplierForSpeed(te.getGeneratedSpeed());
	}

	public float getActualStressOf(KineticTileEntity te) {
		return members.get(te) * getStressMultiplierForSpeed(te.getTheoreticalSpeed());
	}

	private static float getStressMultiplierForSpeed(float speed) {
		return Math.abs(speed);
	}

	public int getSize() {
		return unloadedMembers + members.size();
	}

}
