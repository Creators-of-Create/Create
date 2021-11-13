package com.simibubi.create.lib.event;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;

@Environment(EnvType.CLIENT)
public final class ClientWorldEvents {
	public static final Event<Load> LOAD = EventFactory.createArrayBacked(Load.class, callbacks -> (client, world) -> {
		for (Load callback : callbacks) {
			callback.onWorldLoad(client, world);
		}
	});

	public static final Event<Unload> UNLOAD = EventFactory.createArrayBacked(Unload.class, callbacks -> (client, world) -> {
		for (Unload callback : callbacks) {
			callback.onWorldUnload(client, world);
		}
	});

	private ClientWorldEvents() {}

	@FunctionalInterface
	public interface Load {
		void onWorldLoad(Minecraft client, ClientLevel world);
	}

	@FunctionalInterface
	public interface Unload {
		void onWorldUnload(Minecraft client, ClientLevel world);
	}
}
