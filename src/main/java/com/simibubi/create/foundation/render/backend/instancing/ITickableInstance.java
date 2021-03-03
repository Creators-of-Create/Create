package com.simibubi.create.foundation.render.backend.instancing;

public interface ITickableInstance {
    /**
     * Called every frame, this can be used to make more dynamic animations.
     */
    void tick();
}
