package com.simibubi.create.foundation.tileEntity.behaviour;

import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;

public class BehaviourType<T extends TileEntityBehaviour> {

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

}
