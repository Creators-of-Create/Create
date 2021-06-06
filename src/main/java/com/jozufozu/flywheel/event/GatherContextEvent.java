package com.jozufozu.flywheel.event;

import com.jozufozu.flywheel.backend.Backend;

import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.event.lifecycle.IModBusEvent;

public class GatherContextEvent extends Event implements IModBusEvent {

	private final Backend backend;

	public GatherContextEvent(Backend backend) {
		this.backend = backend;
	}

	public Backend getBackend() {
		return backend;
	}
}
