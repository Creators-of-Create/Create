package com.jozufozu.flywheel.light;

@FunctionalInterface
public interface ICoordinateConsumer {
	void consume(int x, int y, int z);
}
