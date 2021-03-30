package com.simibubi.create.foundation.render.backend.core;

import com.simibubi.create.foundation.render.backend.gl.attrib.IAttribSpec;
import com.simibubi.create.foundation.render.backend.gl.attrib.IVertexAttrib;
import com.simibubi.create.foundation.render.backend.gl.attrib.MatrixAttributes;

public enum TransformAttributes implements IVertexAttrib {
    TRANSFORM("aTransform", MatrixAttributes.MAT4),
    NORMAL_MAT("aNormalMat", MatrixAttributes.MAT3),
    ;

    private final String name;
    private final IAttribSpec spec;

    TransformAttributes(String name, IAttribSpec spec) {
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
        return 0;
    }

    @Override
    public int getBufferIndex() {
        return 0;
    }
}
