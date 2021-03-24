package com.simibubi.create.foundation.render.backend.instancing;

public interface ITickableInstance {

    /**
     * Called every tick. This is useful for things that don't have to be smooth,
     * or to recalculate something that would only change after a game tick.
     */
    void tick();
}
