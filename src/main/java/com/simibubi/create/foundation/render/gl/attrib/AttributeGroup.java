package com.simibubi.create.foundation.render.gl.attrib;

import java.util.ArrayList;

public class AttributeGroup {
    private final int divisor;

    private final ArrayList<VertexAttribute> attributes;

    public AttributeGroup(int divisor) {
        this.divisor = divisor;
        this.attributes = new ArrayList<>();
    }

    public AttributeGroup attrib(VertexAttribute attrib) {
        attributes.add(attrib);
        return this;
    }

    public int getDivisor() {
        return divisor;
    }

    public ArrayList<VertexAttribute> getAttributes() {
        return attributes;
    }
}
