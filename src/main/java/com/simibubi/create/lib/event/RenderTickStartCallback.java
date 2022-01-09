package com.simibubi.create.lib.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public interface RenderTickStartCallback {
	Event<RenderTickStartCallback> EVENT = EventFactory.createArrayBacked(RenderTickStartCallback.class, callbacks -> () -> {
		for (RenderTickStartCallback callback : callbacks) {
			callback.tick();
		}
	});

	void tick();
}
