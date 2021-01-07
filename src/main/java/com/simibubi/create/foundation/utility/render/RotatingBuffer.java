package com.simibubi.create.foundation.utility.render;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL40;

import java.nio.Buffer;
import java.nio.ByteBuffer;

public class RotatingBuffer extends InstancedBuffer<RotatingBuffer.InstanceData> {
    public RotatingBuffer(BufferBuilder buf) {
        super(buf);
    }

    @Override
    protected InstanceData newInstance() {
        return new InstanceData();
    }

    @Override
    protected int numAttributes() {
        return 7;
    }

    @Override
    protected void finishBufferingInternal() {
        int floatSize = VertexFormatElement.Type.FLOAT.getSize();
        int stride = floatSize * 10;

        int instanceSize = instanceCount * stride;

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

        for (int i = 3; i <= numAttributes(); i++) {
            GL40.glVertexAttribDivisor(i, 1);
        }

        // Deselect (bind to 0) the VBO
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }

    public static class InstanceData {
        private float x;
        private float y;
        private float z;
        private int packedLight;
        private float rotationalSpeed;
        private float rotationOffset;
        private float rotationAxisX;
        private float rotationAxisY;
        private float rotationAxisZ;

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

        void buffer(ByteBuffer buf) {
            float blockLightCoordinates = LightUtil.getProperBlockLight(packedLight);
            float skyLightCoordinates = LightUtil.getProperSkyLight(packedLight);

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
        }
    }
}
