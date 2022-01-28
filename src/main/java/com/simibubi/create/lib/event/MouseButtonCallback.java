package com.simibubi.create.lib.event;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.InteractionResult;

@Environment(EnvType.CLIENT)
public interface MouseButtonCallback {
	/**
	 * action:
	 * 1 -> press
	 * 0 -> release
	 * 2 -> repeat
	 */
	Event<MouseButtonCallback> EVENT = EventFactory.createArrayBacked(MouseButtonCallback.class, callbacks -> (button, action, mods) -> {
		for (MouseButtonCallback callback : callbacks) {
			InteractionResult result = callback.onMouseButton(button, action, mods);
			if (result != InteractionResult.PASS) return result;
		}
		return InteractionResult.PASS;
	});

	InteractionResult onMouseButton(int button, int action, int mods);
}
