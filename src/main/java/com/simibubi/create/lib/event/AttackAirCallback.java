package com.simibubi.create.lib.event;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.player.LocalPlayer;

@Environment(EnvType.CLIENT)
public interface AttackAirCallback {
	Event<AttackAirCallback> EVENT = EventFactory.createArrayBacked(AttackAirCallback.class, callbacks -> (player) -> {
		for (AttackAirCallback callback : callbacks) {
			callback.onLeftClickAir(player);
		}
	});

	void onLeftClickAir(LocalPlayer player);
}
