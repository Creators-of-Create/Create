package com.simibubi.create.lib.event;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.Minecraft;

@Environment(EnvType.CLIENT)
public interface BeforeFirstReloadCallback {
	public static final Event<BeforeFirstReloadCallback> EVENT = EventFactory.createArrayBacked(BeforeFirstReloadCallback.class, callbacks -> client -> {
		for (BeforeFirstReloadCallback callback : callbacks) {
			callback.beforeFirstReload(client);
		}
	});

	void beforeFirstReload(Minecraft client);
}
