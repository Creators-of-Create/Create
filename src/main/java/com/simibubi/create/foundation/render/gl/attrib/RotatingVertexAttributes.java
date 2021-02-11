package com.simibubi.create.foundation.render.gl.attrib;

public enum RotatingVertexAttributes implements IVertexAttrib {
    VERTEX_POSITION("aPos", CommonAttributes.VEC3),
    NORMAL("aNormal", CommonAttributes.VEC3),
    TEXTURE("aInstancePos", CommonAttributes.VEC3),
    INSTANCE_POSITION("aInstancePos", CommonAttributes.VEC3, 1),
    LIGHT("aLight", CommonAttributes.LIGHT, 1),
    NETWORK_COLOR("aNetworkTint", CommonAttributes.RGB, 1),
    SPEED("aSpeed", CommonAttributes.FLOAT, 1),
    OFFSET("aOffset", CommonAttributes.FLOAT, 1),
    AXIS("aAxis", CommonAttributes.NORMAL, 1),
    ;

    private final String name;
    private final VertexAttribSpec spec;
    private final int divisor;

    RotatingVertexAttributes(String name, VertexAttribSpec spec) {
        this(name, spec, 0);
    }

    RotatingVertexAttributes(String name, VertexAttribSpec spec, int divisor) {
        this.name = name;
        this.spec = spec;
        this.divisor = divisor;
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
        return divisor;
    }
}
