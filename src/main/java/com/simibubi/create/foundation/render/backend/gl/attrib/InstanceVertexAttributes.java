package com.simibubi.create.foundation.render.backend.gl.attrib;

public enum InstanceVertexAttributes implements IVertexAttrib {
    TRANSFORM("aTransform", MatrixAttributes.MAT4),
    NORMAL_MAT("aNormalMat", MatrixAttributes.MAT3),
    LIGHT("aLight", CommonAttributes.LIGHT),
    COLOR("aColor", CommonAttributes.RGBA),
    ;

    private final String name;
    private final IAttribSpec spec;

    InstanceVertexAttributes(String name, IAttribSpec spec) {
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
