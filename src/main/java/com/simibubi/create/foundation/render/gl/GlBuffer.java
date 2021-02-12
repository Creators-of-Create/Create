package com.simibubi.create.foundation.render.gl;

import com.simibubi.create.foundation.render.gl.backend.Backend;
import org.lwjgl.opengl.GL20;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

public class GlBuffer extends GlObject {

    protected final int bufferType;

    public GlBuffer(int bufferType) {
        setHandle(GL20.glGenBuffers());
        this.bufferType = bufferType;
    }

    public int getBufferType() {
        return bufferType;
    }

    public void bind() {
        GL20.glBindBuffer(bufferType, handle());
    }

    public void unbind() {
        GL20.glBindBuffer(bufferType, 0);
    }

    public void with(Consumer<GlBuffer> action) {
        bind();
        action.accept(this);
        unbind();
    }

    public void map(int offset, int length, Consumer<ByteBuffer> upload) {
        Backend.mapBuffer(bufferType, offset, length, upload);
    }

    protected void deleteInternal(int handle) {
        GL20.glDeleteBuffers(handle);
    }
}
