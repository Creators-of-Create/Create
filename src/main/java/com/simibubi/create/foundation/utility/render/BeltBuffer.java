package com.simibubi.create.foundation.utility.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.create.foundation.block.render.SpriteShiftEntry;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
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
        int stride = floatSize * 16;

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

        // render rotation
        GL20.glVertexAttribPointer(4, 3, GL11.GL_FLOAT, false, stride, floatSize * 3L);

        // light map
        GL20.glVertexAttribPointer(5, 2, GL11.GL_FLOAT, false, stride, floatSize * 6L);

        // speed
        GL20.glVertexAttribPointer(6, 1, GL11.GL_FLOAT, false, stride, floatSize * 8L);

        // uv data
        GL20.glVertexAttribPointer(7, 2, GL11.GL_FLOAT, false, stride, floatSize * 9L);

        GL20.glVertexAttribPointer(8, 4, GL11.GL_FLOAT, false, stride, floatSize * 11L);

        GL20.glVertexAttribPointer(9, 1, GL11.GL_FLOAT, false, stride, floatSize * 15L);

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
        private float rotX;
        private float rotY;
        private float rotZ;
        private int packedLight;
        private float rotationalSpeed;
        private float sourceU;
        private float sourceV;
        private float minU;
        private float minV;
        private float maxU;
        private float maxV;
        private float scrollMult;

        public BeltData setPosition(BlockPos pos) {
            this.x = pos.getX();
            this.y = pos.getY();
            this.z = pos.getZ();
            return this;
        }

        public BeltData setRotation(float rotX, float rotY, float rotZ) {
            this.rotX = rotX;
            this.rotY = rotY;
            this.rotZ = rotZ;
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

        public BeltData setScrollTexture(SpriteShiftEntry spriteShift) {
            TextureAtlasSprite source = spriteShift.getOriginal();
            TextureAtlasSprite target = spriteShift.getTarget();

            this.sourceU = source.getMinU();
            this.sourceV = source.getMinV();
            this.minU = target.getMinU();
            this.minV = target.getMinV();
            this.maxU = target.getMaxU();
            this.maxV = target.getMaxV();

            return this;
        }

        public BeltData setScrollMult(float scrollMult) {
            this.scrollMult = scrollMult;
            return this;
        }

        void buffer(ByteBuffer buf) {
            float blockLightCoordinates = LightTexture.getBlockLightCoordinates(packedLight) / (float) 0xF;
            float skyLightCoordinates = LightTexture.getSkyLightCoordinates(packedLight) / (float) 0xF;

            buf.putFloat(x);
            buf.putFloat(y);
            buf.putFloat(z);

            buf.putFloat(rotX);
            buf.putFloat(rotY);
            buf.putFloat(rotZ);

            buf.putFloat(blockLightCoordinates);
            buf.putFloat(skyLightCoordinates);
            buf.putFloat(rotationalSpeed);

            buf.putFloat(sourceU);
            buf.putFloat(sourceV);
            buf.putFloat(minU);
            buf.putFloat(minV);
            buf.putFloat(maxU);
            buf.putFloat(maxV);

            buf.putFloat(scrollMult);
        }
    }
}
