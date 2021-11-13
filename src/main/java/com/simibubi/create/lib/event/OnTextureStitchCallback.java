package com.simibubi.create.lib.event;

import com.simibubi.create.lib.utility.TextureStitchUtil;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

@Environment(EnvType.CLIENT)
public interface OnTextureStitchCallback {
	Event<OnTextureStitchCallback> EVENT = EventFactory.createArrayBacked(OnTextureStitchCallback.class, callbacks -> (util) -> {
		for (OnTextureStitchCallback callback : callbacks) {
			callback.onModelRegistry(util);
		}
	});

	void onModelRegistry(TextureStitchUtil util);
}
