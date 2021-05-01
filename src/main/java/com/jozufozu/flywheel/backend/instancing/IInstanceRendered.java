package com.jozufozu.flywheel.backend.instancing;

public interface IInstanceRendered {
	default boolean shouldRenderAsTE() {
		return false;
	}
}
