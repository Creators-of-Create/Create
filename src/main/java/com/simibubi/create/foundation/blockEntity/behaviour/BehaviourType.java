package com.simibubi.create.foundation.blockEntity.behaviour;

public class BehaviourType<T extends BlockEntityBehaviour> {

	private String name;

	public BehaviourType(String name) {
		this.name = name;
	}

	public BehaviourType() {
		this("");
	}

	public String getName() {
		return name;
	}

	@Override
	public int hashCode() {
		return super.hashCode() * 31 * 493286711; // Better hash table distribution
	}
}
