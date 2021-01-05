package com.simibubi.create.foundation.utility.render;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL40;

import java.nio.Buffer;
import java.nio.ByteBuffer;

public class BeltBuffer extends InstancedBuffer<BeltBuffer.BeltData> {
    public BeltBuffer(BufferBuilder buf) {
        super(buf);
    }

    @Override
    protected BeltData newInstance() {
        return new BeltData();
    }

    @Override
    protected int numAttributes() {
        return 9;
    }

    @Override
    protected void finishBufferingInternal() {
        int floatSize = VertexFormatElement.Type.FLOAT.getSize();
        int intSize = VertexFormatElement.Type.INT.getSize();
        int stride = floatSize * 22;

        int instanceSize = instanceCount * stride;

        ByteBuffer buffer = GLAllocation.createDirectByteBuffer(instanceSize);
        buffer.order(template.order());
        ((Buffer) buffer).limit(instanceSize);

        data.forEach(instanceData -> instanceData.buffer(buffer));
        buffer.rewind();

        GlStateManager.bindBuffers(GL15.GL_ARRAY_BUFFER, instanceVBO);
        GlStateManager.bufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);

        // render position
        GL20.glVertexAttribPointer(3, 3, GL11.GL_FLOAT, false, stride, 0);

        // model matrix
        for (int i = 0; i < 4; i++) {
            GL20.glVertexAttribPointer(4 + i, 4, GL11.GL_FLOAT, false, stride, floatSize * (4 * i + 3));
        }

        // light map
        GL20.glVertexAttribPointer(8, 2, GL11.GL_FLOAT, false, stride, floatSize * 16L);

        // rotational speed and offset
        GL20.glVertexAttribPointer(9, 1, GL11.GL_FLOAT, false, stride, floatSize * 18L);

        for (int i = 3; i <= numAttributes(); i++) {
            GL40.glVertexAttribDivisor(i, 1);
        }

        // Deselect (bind to 0) the VBO
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }

    public static class BeltData {
        private float x;
        private float y;
        private float z;
        private Matrix4f model;
        private int packedLight;
        private float rotationalSpeed;

        public BeltData setPosition(BlockPos pos) {
            this.x = pos.getX();
            this.y = pos.getY();
            this.z = pos.getZ();
            return this;
        }

        public BeltData setModel(Matrix4f model) {
            this.model = model;
            return this;
        }

        public BeltData setPackedLight(int packedLight) {
            this.packedLight = packedLight;
            return this;
        }

        public BeltData setRotationalSpeed(float rotationalSpeed) {
            this.rotationalSpeed = rotationalSpeed;
            return this;
        }

        void buffer(ByteBuffer buf) {
            float blockLightCoordinates = LightTexture.getBlockLightCoordinates(packedLight) / (float) 0xF;
            float skyLightCoordinates = LightTexture.getSkyLightCoordinates(packedLight) / (float) 0xF;

            buf.putFloat(x);
            buf.putFloat(y);
            buf.putFloat(z);

            InstancedBuffer.MATRIX_BUF.rewind();
            model.write(InstancedBuffer.MATRIX_BUF.asFloatBuffer());
            InstancedBuffer.MATRIX_BUF.rewind();

            buf.put(InstancedBuffer.MATRIX_BUF);
            buf.putFloat(blockLightCoordinates);
            buf.putFloat(skyLightCoordinates);
            buf.putFloat(rotationalSpeed);
        }
    }
}
