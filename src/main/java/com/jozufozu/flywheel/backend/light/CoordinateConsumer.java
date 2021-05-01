package com.jozufozu.flywheel.backend.light;

@FunctionalInterface
public interface CoordinateConsumer {
	void consume(int x, int y, int z);
}
