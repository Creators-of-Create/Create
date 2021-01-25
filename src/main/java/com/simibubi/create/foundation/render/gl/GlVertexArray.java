package com.simibubi.create.foundation.render.gl;

import org.lwjgl.opengl.GL30;

public class GlVertexArray extends GlObject {
    public GlVertexArray() {
        setHandle(GL30.glGenVertexArrays());
    }

    public void bind() {
        GL30.glBindVertexArray(handle());
    }

    public void unbind() {
        GL30.glBindVertexArray(0);
    }

    protected void deleteInternal(int handle) {
        GL30.glDeleteVertexArrays(handle);
    }
}
