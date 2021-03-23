package com.simibubi.create.content.contraptions.base;

import com.simibubi.create.foundation.render.backend.gl.attrib.CommonAttributes;
import com.simibubi.create.foundation.render.backend.gl.attrib.IAttribSpec;
import com.simibubi.create.foundation.render.backend.gl.attrib.IVertexAttrib;
import com.simibubi.create.foundation.render.backend.gl.attrib.VertexAttribSpec;

public enum RotatingAttributes implements IVertexAttrib {
    AXIS("aAxis", CommonAttributes.NORMAL),
    ;

    private final String name;
    private final VertexAttribSpec spec;

    RotatingAttributes(String name, VertexAttribSpec spec) {
        this.name = name;
        this.spec = spec;
    }

    @Override
    public String attribName() {
        return name;
    }

    @Override
    public IAttribSpec attribSpec() {
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
