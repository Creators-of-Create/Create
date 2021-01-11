package com.simibubi.create.foundation.utility.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.create.foundation.utility.render.instancing.VertexFormat;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import org.lwjgl.opengl.*;
import org.lwjgl.system.MemoryUtil;

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

    public void delete() {
        RenderWork.enqueue(() -> {
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

        GL40.glDrawElements(GL11.GL_QUADS, vertexCount, GL11.GL_UNSIGNED_SHORT, 0);

        for (int i = 0; i <= FORMAT.getNumAttributes(); i++) {
            GL40.glDisableVertexAttribArray(i);
        }

        GlStateManager.bindBuffers(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);
    }

    private void setup() {
        int stride = FORMAT.getStride();
        int invariantSize = vertexCount * stride;

        GlStateManager.bindBuffers(GL15.GL_ARRAY_BUFFER, 0);
        GlStateManager.bindBuffers(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
        // Deselect (bind to 0) the VAO
        GL30.glBindVertexArray(0);

        try (SafeDirectBuffer constant = new SafeDirectBuffer(invariantSize)) {
            constant.order(template.order());
            constant.limit(invariantSize);

            for (int i = 0; i < vertexCount; i++) {
                constant.putFloat(getX(template, i));
                constant.putFloat(getY(template, i));
                constant.putFloat(getZ(template, i));

                constant.put(getNX(template, i));
                constant.put(getNY(template, i));
                constant.put(getNZ(template, i));

                constant.putFloat(getU(template, i));
                constant.putFloat(getV(template, i));

                constant.put(getR(template, i));
                constant.put(getG(template, i));
                constant.put(getB(template, i));
                constant.put(getA(template, i));
            }
            constant.rewind();

            vao = GL30.glGenVertexArrays();
            GL30.glBindVertexArray(vao);

            ebo = GlStateManager.genBuffers();
            vbo = GlStateManager.genBuffers();

            GlStateManager.bindBuffers(GL15.GL_ARRAY_BUFFER, vbo);
            GlStateManager.bufferData(GL15.GL_ARRAY_BUFFER, constant.getBacking(), GL15.GL_STATIC_DRAW);
            buildEBO(ebo);

            FORMAT.informAttributes(0);
        } catch (Exception e) {

        }

        GlStateManager.bindBuffers(GL15.GL_ARRAY_BUFFER, 0);
        GlStateManager.bindBuffers(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
        // Deselect (bind to 0) the VAO
        GL30.glBindVertexArray(0);
    }
}
