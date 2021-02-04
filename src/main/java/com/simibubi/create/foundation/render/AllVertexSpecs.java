package com.simibubi.create.foundation.render;

import com.simibubi.create.foundation.render.gl.GlPrimitiveType;
import com.simibubi.create.foundation.render.gl.attrib.CommonAttributes;
import com.simibubi.create.foundation.render.gl.attrib.VertexSpec;
import com.simibubi.create.foundation.render.gl.attrib.VertexAttribute;
import com.simibubi.create.foundation.render.gl.attrib.VertexFormat;

import static com.simibubi.create.foundation.render.gl.attrib.VertexAttribute.copy;
import static com.simibubi.create.foundation.render.gl.attrib.CommonAttributes.*;

public class AllVertexSpecs {

    public static final VertexAttribute ROTATION_CENTER = copy("rotationCenter", CommonAttributes.VEC3);
    public static final VertexAttribute SPEED = copy("speed", CommonAttributes.FLOAT);
    public static final VertexAttribute OFFSET = copy("offset", CommonAttributes.FLOAT);
    public static final VertexAttribute TARGET_UV = copy("scrollTexture", CommonAttributes.VEC4);
    public static final VertexAttribute SCROLL_MULT = new VertexAttribute("scrollMult", GlPrimitiveType.BYTE, 1, true);


    public static final VertexFormat FORMAT = new VertexFormat(CommonAttributes.INSTANCE_POSITION, CommonAttributes.LIGHT, CommonAttributes.RGB, SPEED, OFFSET);

    public static final VertexSpec KINETIC = new VertexSpec()
            .attrib(POSITION)
            .attrib(NORMAL)
            .attrib(UV)
            .pushGroup(1) // instance data
            .attrib(INSTANCE_POSITION)
            .attrib(LIGHT)
            .attrib(RGB)
            .attrib(SPEED)
            .attrib(OFFSET);

    public static final VertexSpec BELT = new VertexSpec(KINETIC)
            .attrib(ROTATION)
            .attrib("uv", UV)
            .attrib(TARGET_UV)
            .attrib(SCROLL_MULT);

    public static final VertexSpec ROTATING = new VertexSpec(KINETIC)
            .attrib("rotationAxis", NORMAL);

    public static final VertexSpec ACTOR = new VertexSpec()
            .attrib(POSITION)
            .attrib(NORMAL)
            .attrib(UV)
            .pushGroup(1) // instance data
            .attrib(INSTANCE_POSITION)
            .attrib(LIGHT)
            .attrib(OFFSET)
            .attrib("localRotationAxis", NORMAL)
            .attrib("localRotation", ROTATION)
            .attrib(ROTATION_CENTER);
}
