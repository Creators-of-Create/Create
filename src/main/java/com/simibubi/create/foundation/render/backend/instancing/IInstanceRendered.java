package com.simibubi.create.foundation.render.backend.instancing;

import com.simibubi.create.foundation.render.backend.light.ILightListener;

public interface IInstanceRendered extends ILightListener {
    default boolean shouldRenderAsTE() {
        return false;
    }
}
