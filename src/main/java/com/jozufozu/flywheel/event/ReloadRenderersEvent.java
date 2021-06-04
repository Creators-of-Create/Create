package com.jozufozu.flywheel.event;

import net.minecraft.client.world.ClientWorld;
import net.minecraftforge.eventbus.api.Event;

public class ReloadRenderersEvent extends Event {
	private final ClientWorld world;

	public ReloadRenderersEvent(ClientWorld world) {
		this.world = world;
	}

	public ClientWorld getWorld() {
		return world;
	}
}
