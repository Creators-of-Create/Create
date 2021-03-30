package com.simibubi.create.foundation.render.backend.core;

import com.simibubi.create.foundation.render.backend.gl.attrib.CommonAttributes;
import com.simibubi.create.foundation.render.backend.gl.attrib.IAttribSpec;
import com.simibubi.create.foundation.render.backend.gl.attrib.IVertexAttrib;

public enum OrientedAttributes implements IVertexAttrib {
    INSTANCE_POS("aInstancePos", CommonAttributes.VEC3),
    PIVOT("aPivot", CommonAttributes.VEC3),
    ROTATION("aRotation", CommonAttributes.QUATERNION),
    ;

    private final String name;
    private final IAttribSpec spec;

    OrientedAttributes(String name, IAttribSpec spec) {
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
