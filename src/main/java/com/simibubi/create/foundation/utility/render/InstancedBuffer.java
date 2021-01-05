package com.simibubi.create.foundation.utility.render;


import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.create.CreateClient;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import org.lwjgl.opengl.*;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.function.Consumer;

public abstract class InstancedBuffer<T> extends TemplateBuffer {

    protected int vao, ebo, invariantVBO, instanceVBO, instanceCount;

    protected final ArrayList<T> data = new ArrayList<>();
    protected boolean shouldBuild = true;

    public InstancedBuffer(BufferBuilder buf) {
        super(buf);
        setupMainData();
    }

    private void setupMainData() {
        int floatSize = VertexFormatElement.Type.FLOAT.getSize();

        int stride = floatSize * 8;
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

            indices.putShort((short) i);
        }
        constant.rewind();
        indices.rewind();

        vao = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vao);

        ebo = GlStateManager.genBuffers();
        invariantVBO = GlStateManager.genBuffers();
        instanceVBO = GlStateManager.genBuffers();

        GlStateManager.bindBuffers(GL15.GL_ARRAY_BUFFER, invariantVBO);
        GlStateManager.bufferData(GL15.GL_ARRAY_BUFFER, constant, GL15.GL_STATIC_DRAW);

        GlStateManager.bindBuffers(GL15.GL_ELEMENT_ARRAY_BUFFER, ebo);
        GlStateManager.bufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indices, GL15.GL_STATIC_DRAW);

        // vertex positions
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, stride, 0);

        // vertex normals
        GL20.glVertexAttribPointer(1, 3, GL11.GL_FLOAT, false, stride, floatSize * 3L);

        // uv position
        GL20.glVertexAttribPointer(2, 2, GL11.GL_FLOAT, false, stride, floatSize * 6L);

        GlStateManager.bindBuffers(GL15.GL_ARRAY_BUFFER, 0);
        GlStateManager.bindBuffers(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
        // Deselect (bind to 0) the VAO
        GL30.glBindVertexArray(0);
    }

    public int numInstances() {
        return instanceCount;
    }

    public boolean isEmpty() {
        return numInstances() == 0;
    }

    public void clearInstanceData() {
        instanceCount = 0;
        shouldBuild = true;
    }

    public void invalidate() {
        CreateClient.kineticRenderer.enqueue(() -> {
            GL15.glDeleteBuffers(invariantVBO);
            GL15.glDeleteBuffers(instanceVBO);
            GL15.glDeleteBuffers(ebo);
            GL30.glDeleteVertexArrays(vao);

            clearInstanceData();
        });
    }

    protected abstract T newInstance();

    protected abstract int numAttributes();

    public void setupInstance(Consumer<T> setup) {
        if (!shouldBuild) return;

        T instanceData = newInstance();
        setup.accept(instanceData);

        data.add(instanceData);
        instanceCount++;
    }

    public void render() {

        GL30.glBindVertexArray(vao);
        finishBuffering();

        for (int i = 0; i <= 10; i++) {
            GL40.glEnableVertexAttribArray(i);
        }

        GlStateManager.bindBuffers(GL15.GL_ELEMENT_ARRAY_BUFFER, ebo);

        GL40.glDrawElementsInstanced(GL11.GL_QUADS, count, GL11.GL_UNSIGNED_SHORT, 0, instanceCount);

        for (int i = 0; i <= 10; i++) {
            GL40.glDisableVertexAttribArray(i);
        }

        GL30.glBindVertexArray(0);
    }

    private void finishBuffering() {
        if (!shouldBuild) return;

        finishBufferingInternal();

        shouldBuild = false;
        data.clear();
    }

    protected abstract void finishBufferingInternal();
}
