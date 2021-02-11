package com.simibubi.create.foundation.render.gl.attrib;

public enum RotatingVertexAttributes implements IVertexAttrib {
    INSTANCE_POSITION("aInstancePos", CommonAttributes.VEC3),
    LIGHT("aLight", CommonAttributes.LIGHT),
    NETWORK_COLOR("aNetworkTint", CommonAttributes.RGB),
    SPEED("aSpeed", CommonAttributes.FLOAT),
    OFFSET("aOffset", CommonAttributes.FLOAT),
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
