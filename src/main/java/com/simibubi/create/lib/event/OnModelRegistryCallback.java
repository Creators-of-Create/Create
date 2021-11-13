package com.simibubi.create.lib.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public interface OnModelRegistryCallback {
	Event<OnModelRegistryCallback> EVENT = EventFactory.createArrayBacked(OnModelRegistryCallback.class, callbacks -> () -> {
		for (OnModelRegistryCallback callback : callbacks) {
			callback.onModelRegistry();
		}
	});

	void onModelRegistry();
}
