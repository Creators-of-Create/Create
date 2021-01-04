package com.simibubi.create.foundation.utility.render;


import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.create.CreateClient;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.*;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.function.Consumer;

public class InstancedBuffer extends TemplateBuffer {

    public int vao, ebo, invariantVBO, instanceVBO, instanceCount;

    private final ArrayList<InstanceData> data = new ArrayList<>();
    private boolean shouldBuild = true;

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

    public void setupInstance(Consumer<InstanceData> setup) {
        if (!shouldBuild) return;

        InstanceData instanceData = new InstanceData();
        setup.accept(instanceData);

        data.add(instanceData);
        instanceCount++;
    }

    public void render() {

        GL30.glBindVertexArray(vao);
        if (finishBuffering()) {

            for (int i = 0; i <= 8; i++) {
                GL40.glEnableVertexAttribArray(i);
            }

            GlStateManager.bindBuffers(GL15.GL_ELEMENT_ARRAY_BUFFER, ebo);

            GL40.glDrawElementsInstanced(GL11.GL_QUADS, count, GL11.GL_UNSIGNED_SHORT, 0, instanceCount);

            for (int i = 0; i <= 8; i++) {
                GL40.glDisableVertexAttribArray(i);
            }
        }
        GL30.glBindVertexArray(0);
    }

    private boolean finishBuffering() {
        if (!shouldBuild) return true;

        int floatSize = VertexFormatElement.Type.FLOAT.getSize();
        int intSize = VertexFormatElement.Type.INT.getSize();
        int stride = floatSize * 10 + intSize * 2;

        int instanceSize = data.size() * stride;

        if (instanceSize == 0) return false;

        ByteBuffer buffer = GLAllocation.createDirectByteBuffer(instanceSize);
        buffer.order(template.order());
        ((Buffer) buffer).limit(instanceSize);

        data.forEach(instanceData -> instanceData.buffer(buffer));
        buffer.rewind();

        GlStateManager.bindBuffers(GL15.GL_ARRAY_BUFFER, instanceVBO);
        GlStateManager.bufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);

        // the render position
        GL20.glVertexAttribPointer(3, 3, GL11.GL_FLOAT, false, stride, 0);

        // vertex lighting
        GL20.glVertexAttribPointer(4, 2, GL11.GL_FLOAT, false, stride, floatSize * 3L);

        // rotational speed and offset
        GL20.glVertexAttribPointer(5, 1, GL11.GL_FLOAT, false, stride, floatSize * 5L);
        GL20.glVertexAttribPointer(6, 1, GL11.GL_FLOAT, false, stride, floatSize * 6L);
        // rotation axis
        GL20.glVertexAttribPointer(7, 3, GL11.GL_FLOAT, false, stride, floatSize * 7L);
        // uv scrolling
        GL20.glVertexAttribPointer(8, 2, GL11.GL_INT, false, stride, floatSize * 10L);

        for (int i = 3; i <= 8; i++) {
            GL40.glVertexAttribDivisor(i, 1);
        }

        // Deselect (bind to 0) the VBO
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

        shouldBuild = false;
        data.clear();

        return true;
    }

    public static class InstanceData {
        private float x;
        private float y;
        private float z;
        private int packedLight = 0;
        private float rotationalSpeed;
        private float rotationOffset;
        private float rotationAxisX;
        private float rotationAxisY;
        private float rotationAxisZ;
        private int cycleLength;
        private int cycleOffset;

        public InstanceData setPackedLight(int packedLight) {
            this.packedLight = packedLight;
            return this;
        }

        public InstanceData setRotationalSpeed(float rotationalSpeed) {
            this.rotationalSpeed = rotationalSpeed;
            return this;
        }

        public InstanceData setRotationOffset(float rotationOffset) {
            this.rotationOffset = rotationOffset;
            return this;
        }

        public InstanceData setPosition(Vector3f pos) {
            this.x = pos.getX();
            this.y = pos.getY();
            this.z = pos.getZ();
            return this;
        }

        public InstanceData setPosition(BlockPos pos) {
            this.x = pos.getX();
            this.y = pos.getY();
            this.z = pos.getZ();
            return this;
        }

        public InstanceData setRotationAxis(Vector3f axis) {
            this.rotationAxisX = axis.getX();
            this.rotationAxisY = axis.getY();
            this.rotationAxisZ = axis.getZ();
            return this;
        }

        public InstanceData setRotationAxis(float rotationAxisX, float rotationAxisY, float rotationAxisZ) {
            this.rotationAxisX = rotationAxisX;
            this.rotationAxisY = rotationAxisY;
            this.rotationAxisZ = rotationAxisZ;
            return this;
        }

        public InstanceData setCycleLength(int cycleLength) {
            this.cycleLength = cycleLength;
            return this;
        }

        public InstanceData setCycleOffset(int cycleOffset) {
            this.cycleOffset = cycleOffset;
            return this;
        }

        void buffer(ByteBuffer buf) {
            float blockLightCoordinates = LightTexture.getBlockLightCoordinates(packedLight) / (float) 0xF;
            float skyLightCoordinates = LightTexture.getSkyLightCoordinates(packedLight) / (float) 0xF;

            buf.putFloat(x);
            buf.putFloat(y);
            buf.putFloat(z);
            buf.putFloat(blockLightCoordinates);
            buf.putFloat(skyLightCoordinates);
            buf.putFloat(rotationalSpeed);
            buf.putFloat(rotationOffset);
            buf.putFloat(rotationAxisX);
            buf.putFloat(rotationAxisY);
            buf.putFloat(rotationAxisZ);
            buf.putInt(cycleLength);
            buf.putInt(cycleOffset);
        }
    }
}
