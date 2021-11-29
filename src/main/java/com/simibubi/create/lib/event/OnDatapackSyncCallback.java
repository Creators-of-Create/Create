package com.simibubi.create.lib.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

import javax.annotation.Nullable;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;

@FunctionalInterface
public interface OnDatapackSyncCallback {
	Event<OnDatapackSyncCallback> EVENT = EventFactory.createArrayBacked(OnDatapackSyncCallback.class, callbacks -> ((playerList, player) -> {
		for (OnDatapackSyncCallback event : callbacks)
			event.onDatapackSync(playerList, player);
	}));

	void onDatapackSync(PlayerList playerList, @Nullable ServerPlayer player);
}
