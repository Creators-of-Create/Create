package com.simibubi.create.foundation.render.backend.instancing;

public interface IDynamicInstance {
    /**
     * Called every frame. This can be used to smoothly change instance data
     * to allow for fancy animations that could not be achieved on the GPU alone.
     */
    void beginFrame();
}
