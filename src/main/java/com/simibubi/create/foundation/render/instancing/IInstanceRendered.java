package com.simibubi.create.foundation.render.instancing;

import com.simibubi.create.foundation.render.light.ILightListener;

public interface IInstanceRendered extends ILightListener {
    default boolean shouldRenderAsTE() {
        return false;
    }
}
