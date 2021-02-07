package com.simibubi.create.foundation.render.gl.backend;

import org.lwjgl.opengl.GLCapabilities;


public interface GlVersioned {
    boolean supported(GLCapabilities caps);
}
