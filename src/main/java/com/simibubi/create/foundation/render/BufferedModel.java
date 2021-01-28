package com.simibubi.create.foundation.render;

import com.simibubi.create.foundation.render.gl.Backend;
import com.simibubi.create.foundation.render.gl.GlBuffer;
import com.simibubi.create.foundation.render.gl.GlVertexArray;
import com.simibubi.create.foundation.render.instancing.VertexFormat;
import net.minecraft.client.renderer.BufferBuilder;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;

import java.nio.ByteBuffer;

public abstract class BufferedModel extends TemplateBuffer {

    protected GlVertexArray vao;

    protected GlBuffer ebo;
    protected GlBuffer invariantVBO;
    protected boolean removed;

    public BufferedModel(BufferBuilder buf) {
        super(buf);
        if (vertexCount > 0) setup();
    }

    protected void setup() {
        int stride = getModelFormat().getStride();

        int invariantSize = vertexCount * stride;

        vao = new GlVertexArray();
        invariantVBO = new GlBuffer();
        ebo = createEBO();

        vao.bind();

        int numAttributes = getTotalShaderAttributeCount();
        for (int i = 0; i <= numAttributes; i++) {
            GL20.glEnableVertexAttribArray(i);
        }

        invariantVBO.bind(GL15.GL_ARRAY_BUFFER);

        // allocate the buffer on the gpu
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, invariantSize, GL15.GL_STATIC_DRAW);

        // mirror it in system memory so we can write to it
        Backend.MAP_BUFFER.mapBuffer(GL15.GL_ARRAY_BUFFER, invariantSize, buffer -> {
            for (int i = 0; i < vertexCount; i++) {
                copyVertex(buffer, i);
            }
        });

        getModelFormat().informAttributes(0);

        invariantVBO.unbind(GL15.GL_ARRAY_BUFFER);
        // Deselect (bind to 0) the VAO
        vao.unbind();
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
        if (vertexCount == 0 || removed) return;

        vao.bind();
        preDrawTask();

        ebo.bind(GL15.GL_ELEMENT_ARRAY_BUFFER);

        drawCall();

        ebo.unbind(GL15.GL_ELEMENT_ARRAY_BUFFER);
        vao.unbind();
    }

    public void delete() {
        removed = true;
        if (vertexCount > 0) {
            RenderWork.enqueue(this::deleteInternal);
        }
    }

    protected void deleteInternal() {
        invariantVBO.delete();
        ebo.delete();
        vao.delete();
    }
}
