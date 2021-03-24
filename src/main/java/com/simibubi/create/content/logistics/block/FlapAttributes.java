package com.simibubi.create.content.logistics.block;

import com.simibubi.create.foundation.render.backend.gl.attrib.CommonAttributes;
import com.simibubi.create.foundation.render.backend.gl.attrib.IAttribSpec;
import com.simibubi.create.foundation.render.backend.gl.attrib.IVertexAttrib;
import com.simibubi.create.foundation.render.backend.gl.attrib.VertexAttribSpec;

public enum FlapAttributes implements IVertexAttrib {
    INSTANCE_POSITION("aInstancePos",CommonAttributes.VEC3),
    LIGHT("aLight", CommonAttributes.LIGHT),
    SEGMENT_OFFSET("aSegmentOffset", CommonAttributes.VEC3),
    PIVOT("aPivot", CommonAttributes.VEC3),
    HORIZONTAL_ANGLE("aHorizontalAngle", CommonAttributes.FLOAT),
    INTENSITY("aIntensity", CommonAttributes.FLOAT),
    FLAP_SCALE("aFlapScale", CommonAttributes.FLOAT),
    FLAPNESS("aFlapness", CommonAttributes.FLOAT),
    ;

    private final String name;
    private final VertexAttribSpec spec;

    FlapAttributes(String name, VertexAttribSpec spec) {
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
