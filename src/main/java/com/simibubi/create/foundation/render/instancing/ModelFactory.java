package com.simibubi.create.foundation.render.instancing;

import net.minecraft.client.renderer.BufferBuilder;

@FunctionalInterface
public interface ModelFactory<B extends InstancedModel<?>> {
    B convert(BufferBuilder buf);
}
