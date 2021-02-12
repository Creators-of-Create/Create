package com.simibubi.create.content.contraptions.components.structureMovement.render;

import com.simibubi.create.foundation.render.backend.BufferedModel;
import com.simibubi.create.foundation.render.backend.gl.attrib.VertexFormat;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.LightTexture;

import java.nio.ByteBuffer;

public class ContraptionModel extends BufferedModel {
    public static final VertexFormat FORMAT = VertexFormat.builder()
                                                             .addAttributes(ContraptionVertexAttributes.class)
                                                             .build();

    public ContraptionModel(BufferBuilder buf) {
        super(buf);
    }

    @Override
    protected void copyVertex(ByteBuffer to, int vertex) {
        to.putFloat(getX(template, vertex));
        to.putFloat(getY(template, vertex));
        to.putFloat(getZ(template, vertex));

        to.put(getNX(template, vertex));
        to.put(getNY(template, vertex));
        to.put(getNZ(template, vertex));

        to.putFloat(getU(template, vertex));
        to.putFloat(getV(template, vertex));

        to.put(getR(template, vertex));
        to.put(getG(template, vertex));
        to.put(getB(template, vertex));
        to.put(getA(template, vertex));

        int light = getLight(template, vertex);

        byte sky = (byte) (LightTexture.getSkyLightCoordinates(light) << 4);
        byte block = (byte) (LightTexture.getBlockLightCoordinates(light) << 4);

        to.put(block);
        to.put(sky);
    }

    @Override
    protected VertexFormat getModelFormat() {
        return FORMAT;
    }
}
