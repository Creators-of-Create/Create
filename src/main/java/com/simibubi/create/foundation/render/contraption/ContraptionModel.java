package com.simibubi.create.foundation.render.contraption;

import com.simibubi.create.foundation.render.BufferedModel;
import com.simibubi.create.foundation.render.instancing.InstancedModel;
import com.simibubi.create.foundation.render.gl.attrib.VertexFormat;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.LightTexture;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL40;

import java.nio.ByteBuffer;

import static com.simibubi.create.foundation.render.gl.attrib.CommonAttributes.LIGHT;
import static com.simibubi.create.foundation.render.gl.attrib.CommonAttributes.RGBA;

public class ContraptionModel extends BufferedModel {
    public static final VertexFormat FORMAT = new VertexFormat(InstancedModel.FORMAT, RGBA, LIGHT);

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

    @Override
    protected void drawCall() {
        GL40.glDrawElements(GL11.GL_QUADS, vertexCount, GL11.GL_UNSIGNED_SHORT, 0);
    }
}
