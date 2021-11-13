package com.simibubi.create.lib.event;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

@Environment(EnvType.CLIENT)
public interface MouseScrolledCallback {
	public static final Event<MouseScrolledCallback> EVENT = EventFactory.createArrayBacked(MouseScrolledCallback.class, callbacks -> delta -> {
		for (MouseScrolledCallback callback : callbacks) {
			if (callback.onMouseScrolled(delta)) {
				return true;
			}
		}
		return false;
	});

	boolean onMouseScrolled(double delta);
}
