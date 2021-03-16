package com.simibubi.create.content.contraptions.components.actors;

import com.simibubi.create.foundation.render.backend.gl.attrib.CommonAttributes;
import com.simibubi.create.foundation.render.backend.gl.attrib.IAttribSpec;
import com.simibubi.create.foundation.render.backend.gl.attrib.IVertexAttrib;
import com.simibubi.create.foundation.render.backend.gl.attrib.VertexAttribSpec;

public enum ActorVertexAttributes implements IVertexAttrib {
    INSTANCE_POSITION("aInstancePos", CommonAttributes.VEC3),
    LIGHT("aModelLight", CommonAttributes.LIGHT),
    OFFSET("aOffset", CommonAttributes.FLOAT),
    AXIS("aAxis", CommonAttributes.NORMAL),
    INSTANCE_ROTATION("aInstanceRot", CommonAttributes.VEC3),
    ROTATION_CENTER("aRotationCenter", CommonAttributes.NORMAL),
    SPEED("aSpeed", CommonAttributes.FLOAT),
    ;

    private final String name;
    private final VertexAttribSpec spec;

    ActorVertexAttributes(String name, VertexAttribSpec spec) {
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
