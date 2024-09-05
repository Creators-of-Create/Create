package com.simibubi.create.compat.computercraft.events;

public class KineticsChangeEvent implements ComputerEvent {

	public float speed;
	public float capacity;
	public float stress;
	public boolean overStressed;

	public KineticsChangeEvent(float speed, float capacity, float stress, boolean overStressed) {
		this.speed = speed;
		this.capacity = capacity;
		this.stress = stress;
		this.overStressed = overStressed;
	}

}
