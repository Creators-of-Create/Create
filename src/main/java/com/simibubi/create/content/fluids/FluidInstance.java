package com.simibubi.create.content.fluids;

import dev.engine_room.flywheel.api.instance.InstanceHandle;
import dev.engine_room.flywheel.api.instance.InstanceType;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;

public class FluidInstance extends TransformedInstance {

	public float progress;
	public float vScale;
	public float v0;

	public FluidInstance(InstanceType<? extends FluidInstance> type, InstanceHandle handle) {
		super(type, handle);
	}
}
