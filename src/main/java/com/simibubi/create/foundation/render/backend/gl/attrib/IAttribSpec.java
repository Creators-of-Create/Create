package com.simibubi.create.foundation.render.backend.gl.attrib;

import org.lwjgl.opengl.GL20;

public interface IAttribSpec {

    void vertexAttribPointer(int stride, int index, int pointer);

    int getSize();

    int getAttributeCount();
}
