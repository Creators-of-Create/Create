package com.simibubi.create.foundation.render.backend.gl;

import java.util.function.Consumer;

import com.simibubi.create.foundation.render.backend.Backend;
import org.lwjgl.opengl.GL30;

public class GlVertexArray extends GlObject {
    public GlVertexArray() {
        setHandle(Backend.functions.genVertexArrays());
    }

    public void bind() {
        Backend.functions.bindVertexArray(handle());
    }

    public void unbind() {
        Backend.functions.bindVertexArray(0);
    }

    public void with(Consumer<GlVertexArray> action) {
        bind();
        action.accept(this);
        unbind();
    }

    protected void deleteInternal(int handle) {
        Backend.functions.deleteVertexArrays(handle);
    }
}
