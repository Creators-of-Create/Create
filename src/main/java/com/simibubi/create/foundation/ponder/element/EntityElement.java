package com.simibubi.create.foundation.ponder.element;

import net.minecraft.world.entity.Entity;

public class EntityElement extends TrackedElement<Entity> {

	public EntityElement(Entity wrapped) {
		super(wrapped);
	}

	@Override
	protected boolean isStillValid(Entity element) {
		return element.isAlive();
	}

}
