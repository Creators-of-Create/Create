package com.simibubi.create.lib.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;

public interface StartRidingCallback {
	public static final Event<StartRidingCallback> EVENT = EventFactory.createArrayBacked(StartRidingCallback.class, callbacks -> (mounted, mounting) -> {
		InteractionResult result = InteractionResult.PASS;
		for (StartRidingCallback callback : callbacks) {
			result = callback.onStartRiding(mounted, mounting);
			if (result == InteractionResult.FAIL) {
				return result;
			}
		}
		return result;
	});

	InteractionResult onStartRiding(Entity mounted, Entity mounting);
}
