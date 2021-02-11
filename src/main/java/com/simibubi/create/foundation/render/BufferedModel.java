package com.simibubi.create.foundation.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.create.foundation.render.gl.backend.Backend;
import com.simibubi.create.foundation.render.gl.GlBuffer;
import com.simibubi.create.foundation.render.gl.attrib.VertexFormat;
import net.minecraft.client.renderer.BufferBuilder;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;

import java.nio.ByteBuffer;
import java.util.HashSet;

public abstract class BufferedModel extends TemplateBuffer {

    protected GlBuffer modelVBO;
    protected boolean removed;

    public BufferedModel(BufferBuilder buf) {
        super(buf);
        if (vertexCount > 0) init();
    }

    protected void init() {

        modelVBO = new GlBuffer(GL20.GL_ARRAY_BUFFER);

        modelVBO.bind();
        initModel();
        modelVBO.unbind();
    }

    protected void initModel() {
        int stride = getModelFormat().getStride();
        int invariantSize = vertexCount * stride;

        // allocate the buffer on the gpu
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, invariantSize, GL15.GL_STATIC_DRAW);

        // mirror it in system memory so we can write to it
        Backend.mapBuffer(GL15.GL_ARRAY_BUFFER, invariantSize, buffer -> {
            for (int i = 0; i < vertexCount; i++) {
                copyVertex(buffer, i);
            }
        });
    }

    protected abstract void copyVertex(ByteBuffer to, int index);

    protected abstract VertexFormat getModelFormat();

    protected int getTotalShaderAttributeCount() {
        return getModelFormat().getShaderAttributeCount();
    }

    /**
     * Renders this model, checking first if it should actually be rendered.
     */
    public final void render() {
        if (vertexCount == 0 || removed) return;

        doRender();
    }

    /**
     * Override this
     */
    protected void doRender() {
        GL20.glDisableClientState(32884);
        GL20.glDisableClientState(32885);
        GL20.glDisableClientState(32886);
        GL20.glDisableClientState(32888);
        GL20.glEnable(GL20.GL_VERTEX_ARRAY);
        modelVBO.bind();

        setupAttributes();
        GL20.glDrawArrays(GL11.GL_QUADS, 0, vertexCount);

        modelVBO.unbind();

        int numAttributes = getTotalShaderAttributeCount();
        for (int i = 0; i <= numAttributes; i++) {
            GL20.glDisableVertexAttribArray(i);
        }

        GL20.glDisable(GL20.GL_VERTEX_ARRAY);
    }

    protected void setupAttributes() {
        getModelFormat().informAttributes(0);

        int numAttributes = getTotalShaderAttributeCount();
        for (int i = 0; i <= numAttributes; i++) {
            GL20.glEnableVertexAttribArray(i);
        }
    }

    public void delete() {
        removed = true;
        if (vertexCount > 0) {
            RenderWork.enqueue(this::deleteInternal);
        }
    }

    protected void deleteInternal() {
        modelVBO.delete();
    }
}
