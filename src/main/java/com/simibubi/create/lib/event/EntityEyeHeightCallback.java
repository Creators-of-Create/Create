package com.simibubi.create.lib.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.entity.Entity;

public interface EntityEyeHeightCallback {
	public static final Event<EntityEyeHeightCallback> EVENT = EventFactory.createArrayBacked(EntityEyeHeightCallback.class, callbacks -> (entity) -> {
		for (EntityEyeHeightCallback callback : callbacks) {
			return callback.onEntitySize(entity);
		}

		return -1;
	});

	int onEntitySize(Entity entity);
}
