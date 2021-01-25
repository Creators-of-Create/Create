package com.simibubi.create.foundation.render.gl;

import org.lwjgl.opengl.GL20;

public class GlBuffer extends GlObject {
    public GlBuffer() {
        setHandle(GL20.glGenBuffers());
    }

    public void bind(int target) {
        GL20.glBindBuffer(target, handle());
    }

    public void unbind(int target) {
        GL20.glBindBuffer(target, 0);
    }

    protected void deleteInternal(int handle) {
        GL20.glDeleteBuffers(handle);
    }
}
