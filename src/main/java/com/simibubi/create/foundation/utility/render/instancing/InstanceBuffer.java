package com.simibubi.create.foundation.utility.render.instancing;


import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.utility.render.TemplateBuffer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import org.lwjgl.opengl.*;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.function.Consumer;

public abstract class InstanceBuffer<D extends InstanceData> extends TemplateBuffer {

    protected int vao, ebo, invariantVBO, instanceVBO, instanceCount;

    protected final ArrayList<D> data = new ArrayList<>();
    protected boolean shouldBuild = true;

    public InstanceBuffer(BufferBuilder buf) {
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

    protected abstract VertexFormat getInstanceFormat();

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

    protected abstract D newInstance();

    public void setupInstance(Consumer<D> setup) {
        if (!shouldBuild) return;

        D instanceData = newInstance();
        setup.accept(instanceData);

        data.add(instanceData);
        instanceCount++;
    }

    public void render() {

        GL30.glBindVertexArray(vao);
        finishBuffering();

        int numAttributes = getInstanceFormat().getNumAttributes() + 3;
        for (int i = 0; i <= numAttributes; i++) {
            GL40.glEnableVertexAttribArray(i);
        }

        GlStateManager.bindBuffers(GL15.GL_ELEMENT_ARRAY_BUFFER, ebo);

        GL40.glDrawElementsInstanced(GL11.GL_QUADS, count, GL11.GL_UNSIGNED_SHORT, 0, instanceCount);

        for (int i = 0; i <= numAttributes; i++) {
            GL40.glDisableVertexAttribArray(i);
        }

        GlStateManager.bindBuffers(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);
    }

    private void finishBuffering() {
        if (!shouldBuild) return;

        VertexFormat instanceFormat = getInstanceFormat();

        int instanceSize = instanceCount * instanceFormat.getStride();

        ByteBuffer buffer = GLAllocation.createDirectByteBuffer(instanceSize);
        buffer.order(template.order());
        ((Buffer) buffer).limit(instanceSize);

        data.forEach(instanceData -> instanceData.write(buffer));
        buffer.rewind();

        GlStateManager.bindBuffers(GL15.GL_ARRAY_BUFFER, instanceVBO);
        GlStateManager.bufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);

        instanceFormat.informAttributes(3);

        for (int i = 0; i < instanceFormat.getNumAttributes(); i++) {
            GL40.glVertexAttribDivisor(i + 3, 1);
        }

        // Deselect (bind to 0) the VBO
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

        shouldBuild = false;
        data.clear();
    }
}
