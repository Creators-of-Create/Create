package com.simibubi.create.foundation.render.instancing;

public interface IInstanceRendered {
    default boolean shouldRenderAsTE() {
        return false;
    }
}
