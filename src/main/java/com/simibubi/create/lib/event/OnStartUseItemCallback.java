package com.simibubi.create.lib.event;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;

@Environment(EnvType.CLIENT)
public interface OnStartUseItemCallback {
	/**
	 * SUCCESS - swing hand
	 * FAIL - cancel, do nothing
	 * PASS - do nothing
	 */
	Event<OnStartUseItemCallback> EVENT = EventFactory.createArrayBacked(OnStartUseItemCallback.class, callbacks -> (hand) -> {
		for (OnStartUseItemCallback callback : callbacks) {
			InteractionResult result = callback.onStartUse(hand);
			if (result != InteractionResult.PASS) return result;
		}
		return InteractionResult.PASS;
	});

	InteractionResult onStartUse(InteractionHand hand);
}
