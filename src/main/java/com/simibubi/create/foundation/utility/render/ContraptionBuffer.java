package com.simibubi.create.foundation.utility.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.utility.render.instancing.VertexFormat;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import org.lwjgl.opengl.*;
import static com.simibubi.create.foundation.utility.render.instancing.VertexAttribute.*;

import java.nio.Buffer;
import java.nio.ByteBuffer;

public class ContraptionBuffer extends TemplateBuffer {
    public static final VertexFormat FORMAT = new VertexFormat(POSITION, NORMAL, UV, COLOR);

    protected int vao, ebo, vbo;

    public ContraptionBuffer(BufferBuilder buf) {
        super(buf);
        setup();
    }

    public void invalidate() {
        CreateClient.kineticRenderer.enqueue(() -> {
            GL15.glDeleteBuffers(vbo);
            GL15.glDeleteBuffers(ebo);
            GL30.glDeleteVertexArrays(vao);
        });
    }

    public void render() {

        GL30.glBindVertexArray(vao);

        for (int i = 0; i <= 3; i++) {
            GL40.glEnableVertexAttribArray(i);
        }

        GlStateManager.bindBuffers(GL15.GL_ELEMENT_ARRAY_BUFFER, ebo);

        GL40.glDrawElements(GL11.GL_QUADS, count, GL11.GL_UNSIGNED_SHORT, 0);

        for (int i = 0; i <= FORMAT.getNumAttributes(); i++) {
            GL40.glDisableVertexAttribArray(i);
        }

        GlStateManager.bindBuffers(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);
    }

    private void setup() {
        int stride = FORMAT.getStride();
        int invariantSize = count * stride;

        ByteBuffer constant = GLAllocation.createDirectByteBuffer(invariantSize);
        constant.order(template.order());
        ((Buffer) constant).limit(invariantSize);

        int indicesSize = count * VertexFormatElement.Type.USHORT.getSize();
        ByteBuffer indices = GLAllocation.createDirectByteBuffer(indicesSize);
        indices.order(template.order());
        ((Buffer) indices).limit(indicesSize);

        int vertexCount = vertexCount(template);
        for (int i = 0; i < vertexCount; i++) {
            constant.putFloat(getX(template, i));
            constant.putFloat(getY(template, i));
            constant.putFloat(getZ(template, i));

            constant.putFloat(getNX(template, i));
            constant.putFloat(getNY(template, i));
            constant.putFloat(getNZ(template, i));

            constant.putFloat(getU(template, i));
            constant.putFloat(getV(template, i));

            constant.putFloat(getR(template, i) / 255f);
            constant.putFloat(getG(template, i) / 255f);
            constant.putFloat(getB(template, i) / 255f);
            constant.putFloat(getA(template, i) / 255f);

            indices.putShort((short) i);
        }
        constant.rewind();
        indices.rewind();

        vao = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vao);

        ebo = GlStateManager.genBuffers();
        vbo = GlStateManager.genBuffers();

        GlStateManager.bindBuffers(GL15.GL_ARRAY_BUFFER, vbo);
        GlStateManager.bufferData(GL15.GL_ARRAY_BUFFER, constant, GL15.GL_STATIC_DRAW);

        GlStateManager.bindBuffers(GL15.GL_ELEMENT_ARRAY_BUFFER, ebo);
        GlStateManager.bufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indices, GL15.GL_STATIC_DRAW);

        FORMAT.informAttributes(0);

        GlStateManager.bindBuffers(GL15.GL_ARRAY_BUFFER, 0);
        GlStateManager.bindBuffers(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
        // Deselect (bind to 0) the VAO
        GL30.glBindVertexArray(0);
    }
}
