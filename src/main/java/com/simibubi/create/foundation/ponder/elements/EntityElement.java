package com.simibubi.create.foundation.ponder.elements;

import net.minecraft.entity.Entity;

public class EntityElement extends TrackedElement<Entity> {

	public EntityElement(Entity wrapped) {
		super(wrapped);
	}

	@Override
	protected boolean isStillValid(Entity element) {
		return element.isAlive();
	}

}
