package com.simibubi.create.foundation.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.create.foundation.render.instancing.VertexFormat;
import net.minecraft.client.renderer.BufferBuilder;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL40;

import java.nio.ByteBuffer;

public abstract class GPUBuffer extends TemplateBuffer {

    protected int vao, ebo, invariantVBO;

    public GPUBuffer(BufferBuilder buf) {
        super(buf);
        if (vertexCount > 0) setup();
    }

    protected void setup() {
        int stride = getModelFormat().getStride();

        int invariantSize = vertexCount * stride;

        vao = GL30.glGenVertexArrays();
        ebo = GlStateManager.genBuffers();
        invariantVBO = GlStateManager.genBuffers();

        GL30.glBindVertexArray(vao);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, invariantVBO);

        // allocate the buffer on the gpu
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, invariantSize, GL15.GL_STATIC_DRAW);

        // mirror it in system memory so we can write to it
        ByteBuffer constant = GL15.glMapBuffer(GL15.GL_ARRAY_BUFFER, GL15.GL_WRITE_ONLY);

        for (int i = 0; i < vertexCount; i++) {
            copyVertex(constant, i);
        }
        constant.rewind();
        GL15.glUnmapBuffer(GL15.GL_ARRAY_BUFFER);

        buildEBO(ebo);

        getModelFormat().informAttributes(0);

        GlStateManager.bindBuffers(GL15.GL_ARRAY_BUFFER, 0);
        GlStateManager.bindBuffers(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
        // Deselect (bind to 0) the VAO
        GL30.glBindVertexArray(0);
    }

    protected abstract void copyVertex(ByteBuffer to, int index);

    protected abstract VertexFormat getModelFormat();

    protected int getTotalShaderAttributeCount() {
        return getModelFormat().getShaderAttributeCount();
    }

    protected abstract void drawCall();

    protected void preDrawTask() {

    }

    public void render() {
        if (vao == 0) return;

        GL30.glBindVertexArray(vao);
        preDrawTask();

        int numAttributes = getTotalShaderAttributeCount();
        for (int i = 0; i <= numAttributes; i++) {
            GL40.glEnableVertexAttribArray(i);
        }

        GlStateManager.bindBuffers(GL15.GL_ELEMENT_ARRAY_BUFFER, ebo);

        drawCall();

        for (int i = 0; i <= numAttributes; i++) {
            GL40.glDisableVertexAttribArray(i);
        }

        GlStateManager.bindBuffers(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);
    }

    public void delete() {
        if (vertexCount > 0) {
            RenderWork.enqueue(this::deleteInternal);
        }
    }

    protected void deleteInternal() {
        GL15.glDeleteBuffers(invariantVBO);
        GL15.glDeleteBuffers(ebo);
        GL30.glDeleteVertexArrays(vao);
        vao = 0;
        ebo = 0;
        invariantVBO = 0;
    }
}
