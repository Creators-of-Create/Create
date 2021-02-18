package com.simibubi.create.foundation.render.backend.gl;

import java.util.function.Consumer;

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

    public void with(Consumer<GlVertexArray> action) {
        bind();
        action.accept(this);
        unbind();
    }

    protected void deleteInternal(int handle) {
        GL30.glDeleteVertexArrays(handle);
    }
}
