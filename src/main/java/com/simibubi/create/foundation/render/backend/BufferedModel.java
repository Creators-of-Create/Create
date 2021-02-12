package com.simibubi.create.foundation.render.backend;

import com.simibubi.create.foundation.render.RenderWork;
import com.simibubi.create.foundation.render.TemplateBuffer;
import com.simibubi.create.foundation.render.backend.gl.GlPrimitiveType;
import com.simibubi.create.foundation.render.backend.gl.attrib.VertexFormat;
import com.simibubi.create.foundation.render.backend.gl.GlBuffer;
import net.minecraft.client.renderer.BufferBuilder;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;

import java.nio.ByteBuffer;

public abstract class BufferedModel extends TemplateBuffer {

    protected GlBuffer ebo;
    protected GlBuffer modelVBO;
    protected boolean removed;

    public BufferedModel(BufferBuilder buf) {
        super(buf);
        if (vertexCount > 0) init();
    }

    protected void init() {

        modelVBO = new GlBuffer(GL20.GL_ARRAY_BUFFER);

        modelVBO.with(vbo -> initModel());

        ebo = createEBO();
    }

    protected void initModel() {
        int stride = getModelFormat().getStride();
        int invariantSize = vertexCount * stride;

        // allocate the buffer on the gpu
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, invariantSize, GL15.GL_STATIC_DRAW);

        // mirror it in system memory so we can write to it
        modelVBO.map(invariantSize, buffer -> {
            for (int i = 0; i < vertexCount; i++) {
                copyVertex(buffer, i);
            }
        });
    }

    protected final GlBuffer createEBO() {
        GlBuffer ebo = new GlBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER);

        int indicesSize = vertexCount * GlPrimitiveType.USHORT.getSize();

        ebo.bind();

        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indicesSize, GL15.GL_STATIC_DRAW);
        ebo.map(indicesSize, indices -> {
            for (int i = 0; i < vertexCount; i++) {
                indices.putShort((short) i);
            }
        });

        ebo.unbind();

        return ebo;
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
     * Override this.
     */
    protected void doRender() {
        modelVBO.bind();
        ebo.bind();

        setupAttributes();
        GL20.glDrawElements(GL20.GL_QUADS, vertexCount, GlPrimitiveType.USHORT.getGlConstant(), 0);

        int numAttributes = getTotalShaderAttributeCount();
        for (int i = 0; i <= numAttributes; i++) {
            GL20.glDisableVertexAttribArray(i);
        }

        ebo.unbind();
        modelVBO.unbind();
    }

    protected void setupAttributes() {
        int numAttributes = getTotalShaderAttributeCount();
        for (int i = 0; i <= numAttributes; i++) {
            GL20.glEnableVertexAttribArray(i);
        }

        getModelFormat().vertexAttribPointers(0);
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
