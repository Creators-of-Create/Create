package com.simibubi.create.content.contraptions.components.structureMovement.render;

import java.nio.ByteBuffer;

import com.simibubi.create.foundation.render.backend.BufferedModel;
import com.simibubi.create.foundation.render.backend.gl.GlBuffer;
import com.simibubi.create.foundation.render.backend.gl.GlPrimitiveType;
import com.simibubi.create.foundation.render.backend.gl.attrib.VertexFormat;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.LightTexture;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;

public class ContraptionModel extends BufferedModel {
    public static final VertexFormat FORMAT = VertexFormat.builder()
                                                             .addAttributes(ContraptionVertexAttributes.class)
                                                             .build();

    protected GlPrimitiveType eboIndexType;
    protected GlBuffer ebo;

    public ContraptionModel(BufferBuilder buf) {
        super(buf);
    }

    @Override
    protected void init() {
        super.init();

        createEBO();
    }

    @Override
    protected void doRender() {
        modelVBO.bind();
        ebo.bind();

        setupAttributes();
        GL20.glDrawElements(GL20.GL_QUADS, vertexCount, eboIndexType.getGlConstant(), 0);

        int numAttributes = getTotalShaderAttributeCount();
        for (int i = 0; i <= numAttributes; i++) {
            GL20.glDisableVertexAttribArray(i);
        }

        ebo.unbind();
        modelVBO.unbind();
    }

    protected final void createEBO() {
        ebo = new GlBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER);
        eboIndexType = GlPrimitiveType.UINT; // TODO: choose this based on the number of vertices

        int indicesSize = vertexCount * eboIndexType.getSize();

        ebo.bind();

        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indicesSize, GL15.GL_STATIC_DRAW);
        ebo.map(indicesSize, indices -> {
            for (int i = 0; i < vertexCount; i++) {
                indices.putInt(i);
            }
        });

        ebo.unbind();
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
    protected void deleteInternal() {
        super.deleteInternal();
        ebo.delete();
    }
}
