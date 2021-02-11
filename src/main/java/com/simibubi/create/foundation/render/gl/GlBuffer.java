package com.simibubi.create.foundation.render.gl;

import org.lwjgl.opengl.GL20;

public class GlBuffer extends GlObject {

    protected final int bufferType;

    public GlBuffer(int bufferType) {
        setHandle(GL20.glGenBuffers());
        this.bufferType = bufferType;
    }

    public void bind() {
        GL20.glBindBuffer(bufferType, handle());
    }

    public void unbind() {
        GL20.glBindBuffer(bufferType, 0);
    }

    protected void deleteInternal(int handle) {
        GL20.glDeleteBuffers(handle);
    }
}
