package com.jozufozu.flywheel.backend.instancing;

import net.minecraft.client.renderer.BufferBuilder;

@FunctionalInterface
public interface ModelFactory<B extends InstancedModel<?>> {
	B makeModel(InstancedTileRenderer<?> renderer, BufferBuilder buf);
}
