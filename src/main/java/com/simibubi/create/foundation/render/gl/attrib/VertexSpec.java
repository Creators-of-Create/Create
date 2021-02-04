package com.simibubi.create.foundation.render.gl.attrib;

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.function.Consumer;

public class VertexSpec {

    private final ArrayList<AttributeGroup> groups;

    public VertexSpec() {
        groups = Lists.newArrayList(new AttributeGroup(0));
    }

    public VertexSpec(VertexSpec that) {
        groups = new ArrayList<>();
        for (AttributeGroup group : that.groups) {
            AttributeGroup copy = new AttributeGroup(group.getDivisor());

            for (VertexAttribute attribute : group.getAttributes()) {
                copy.attrib(attribute);
            }

            groups.add(copy);
        }
    }

    public VertexSpec pushGroup() {
        return pushGroup(0);
    }

    public VertexSpec group(int divisor, Consumer<AttributeGroup> builder) {
        AttributeGroup group = new AttributeGroup(divisor);
        builder.accept(group);
        return group(group);
    }

    public VertexSpec pushGroup(int divisor) {
        return group(new AttributeGroup(divisor));
    }

    public VertexSpec group(AttributeGroup group) {
        groups.add(group);
        return this;
    }

    public VertexSpec attrib(String name, VertexAttribute attrib) {
        return attrib(VertexAttribute.copy(name, attrib));
    }

    public VertexSpec attrib(VertexAttribute attrib) {
        last().attrib(attrib);
        return this;
    }


    private AttributeGroup last() {
        return groups.get(groups.size() - 1);
    }
}
