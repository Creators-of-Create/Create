package com.simibubi.create.lib.event;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public interface OverlayRenderCallback {
	Event<OverlayRenderCallback> EVENT = EventFactory.createArrayBacked(OverlayRenderCallback.class, callbacks -> (stack, partialTicks, window, type) -> {
		for (OverlayRenderCallback callback : callbacks) {
			callback.onOverlayRender(stack, partialTicks, window, type);
		}
	});

	void onOverlayRender(PoseStack stack, float partialTicks, Window window, Types type);

	enum Types {
		AIR,
		CROSSHAIRS
		;
	}
}
