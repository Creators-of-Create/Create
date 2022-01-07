package com.simibubi.create.lib.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;

import javax.annotation.Nullable;

public interface EntityReadExtraDataCallback {
	Event<EntityReadExtraDataCallback> EVENT = EventFactory.createArrayBacked(EntityReadExtraDataCallback.class, callbacks -> (entity, data) -> {
		for (EntityReadExtraDataCallback callback : callbacks) {
			callback.onLoad(entity, data);
		}
	});

	void onLoad(Entity entity, @Nullable CompoundTag extraData);
}
