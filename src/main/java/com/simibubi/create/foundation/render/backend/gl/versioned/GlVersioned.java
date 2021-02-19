package com.simibubi.create.foundation.render.backend.gl.versioned;

import org.lwjgl.opengl.GLCapabilities;


public interface GlVersioned {
    boolean supported(GLCapabilities caps);
}
