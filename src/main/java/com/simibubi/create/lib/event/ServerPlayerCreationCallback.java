package com.simibubi.create.lib.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.level.ServerPlayer;

public interface ServerPlayerCreationCallback {
	Event<ServerPlayerCreationCallback> EVENT = EventFactory.createArrayBacked(ServerPlayerCreationCallback.class, callbacks -> (player) -> {
		for (ServerPlayerCreationCallback callback : callbacks) {
			callback.onCreate(player);
		}
	});

	void onCreate(ServerPlayer player);
}
