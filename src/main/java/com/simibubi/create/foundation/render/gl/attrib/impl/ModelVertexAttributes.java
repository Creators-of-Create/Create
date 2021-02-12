package com.simibubi.create.foundation.render.gl.attrib.impl;

import com.simibubi.create.foundation.render.gl.attrib.CommonAttributes;
import com.simibubi.create.foundation.render.gl.attrib.IVertexAttrib;
import com.simibubi.create.foundation.render.gl.attrib.VertexAttribSpec;

public enum ModelVertexAttributes implements IVertexAttrib {
    VERTEX_POSITION("aPos", CommonAttributes.VEC3),
    NORMAL("aNormal", CommonAttributes.NORMAL),
    TEXTURE("aTexCoords", CommonAttributes.UV),
    ;

    private final String name;
    private final VertexAttribSpec spec;

    ModelVertexAttributes(String name, VertexAttribSpec spec) {
        this.name = name;
        this.spec = spec;
    }

    @Override
    public String attribName() {
        return name;
    }

    @Override
    public VertexAttribSpec attribSpec() {
        return spec;
    }

    @Override
    public int getDivisor() {
        return 0;
    }

    @Override
    public int getBufferIndex() {
        return 0;
    }
}
