package com.simibubi.create.foundation.render.gl.attrib.impl;

import com.simibubi.create.foundation.render.gl.attrib.CommonAttributes;
import com.simibubi.create.foundation.render.gl.attrib.IVertexAttrib;
import com.simibubi.create.foundation.render.gl.attrib.VertexAttribSpec;

public enum RotatingVertexAttributes implements IVertexAttrib {
    AXIS("aAxis", CommonAttributes.NORMAL),
    ;

    private final String name;
    private final VertexAttribSpec spec;

    RotatingVertexAttributes(String name, VertexAttribSpec spec) {
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
        return 1;
    }

    @Override
    public int getBufferIndex() {
        return 1;
    }
}
