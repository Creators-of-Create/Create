package com.simibubi.create.foundation.render.gl.backend;

public enum RenderingAvailability {
    /**
     * The current system does not support enough
     * OpenGL features to enable fast rendering.
     */
    INCAPABLE,

    /**
     * The current system supports OpenGL 3.3.
     */
    FULL,

    /**
     * The current system supports OpenGL 2.0,
     * or some ARBs that make it equivalent.
     */
    PARTIAL,

    /**
     * It doesn't matter what the current system
     * supports because Optifine is installed and
     * a shaderpack is enabled.
     */
    OPTIFINE_SHADERS,
}
