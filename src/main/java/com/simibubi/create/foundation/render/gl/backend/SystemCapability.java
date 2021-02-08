package com.simibubi.create.foundation.render.gl.backend;

public enum SystemCapability {
    /**
     * The current system does not support enough
     * OpenGL features to enable fast rendering.
     */
    INCAPABLE,

    /**
     * The current system supports OpenGL 3.3.
     */
    CAPABLE,
    ;

    public boolean isCapable() {
        return this == CAPABLE;
    }
}
