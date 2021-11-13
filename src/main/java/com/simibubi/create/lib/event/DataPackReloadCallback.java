package com.simibubi.create.lib.event;

import java.util.ArrayList;
import java.util.List;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.ServerResources;
import net.minecraft.server.packs.resources.PreparableReloadListener;

public interface DataPackReloadCallback {
	public static final Event<DataPackReloadCallback> EVENT = EventFactory.createArrayBacked(DataPackReloadCallback.class, callbacks -> registry -> {
		List<PreparableReloadListener> listeners = new ArrayList<>();
		for (DataPackReloadCallback callback : callbacks) {
			listeners.addAll(callback.onDataPackReload(registry));
		}
		return listeners;
	});

	List<PreparableReloadListener> onDataPackReload(ServerResources dataPackRegistries);
}
