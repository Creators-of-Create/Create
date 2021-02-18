package com.simibubi.create.foundation.render.backend.light;

@FunctionalInterface
public interface CoordinateConsumer {
    void consume(int x, int y, int z);
}
