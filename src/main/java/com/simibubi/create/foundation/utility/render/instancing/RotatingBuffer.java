package com.simibubi.create.foundation.utility.render.instancing;

import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.create.foundation.utility.render.LightUtil;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL40;

import java.nio.Buffer;
import java.nio.ByteBuffer;

import static com.simibubi.create.foundation.utility.render.instancing.VertexAttribute.*;
import static com.simibubi.create.foundation.utility.render.instancing.VertexAttribute.FLOAT;

public class RotatingBuffer extends InstanceBuffer<RotatingBuffer.RotatingData> {
    public RotatingBuffer(BufferBuilder buf) {
        super(buf);
    }

    @Override
    protected RotatingData newInstance() {
        return new RotatingData();
    }

    @Override
    protected VertexFormat getInstanceFormat() {
        return RotatingData.FORMAT;
    }

    public static class RotatingData extends InstanceData {
        public static VertexFormat FORMAT = new VertexFormat(VEC3, VEC2, FLOAT, FLOAT, VEC3);

        private float x;
        private float y;
        private float z;
        private int packedLight;
        private float rotationalSpeed;
        private float rotationOffset;
        private float rotationAxisX;
        private float rotationAxisY;
        private float rotationAxisZ;

        public RotatingData setPackedLight(int packedLight) {
            this.packedLight = packedLight;
            return this;
        }

        public RotatingData setRotationalSpeed(float rotationalSpeed) {
            this.rotationalSpeed = rotationalSpeed;
            return this;
        }

        public RotatingData setRotationOffset(float rotationOffset) {
            this.rotationOffset = rotationOffset;
            return this;
        }

        public RotatingData setPosition(Vector3f pos) {
            this.x = pos.getX();
            this.y = pos.getY();
            this.z = pos.getZ();
            return this;
        }

        public RotatingData setPosition(BlockPos pos) {
            this.x = pos.getX();
            this.y = pos.getY();
            this.z = pos.getZ();
            return this;
        }

        public RotatingData setRotationAxis(Vector3f axis) {
            this.rotationAxisX = axis.getX();
            this.rotationAxisY = axis.getY();
            this.rotationAxisZ = axis.getZ();
            return this;
        }

        public RotatingData setRotationAxis(float rotationAxisX, float rotationAxisY, float rotationAxisZ) {
            this.rotationAxisX = rotationAxisX;
            this.rotationAxisY = rotationAxisY;
            this.rotationAxisZ = rotationAxisZ;
            return this;
        }

        @Override
        public void write(ByteBuffer buf) {
            float blockLightCoordinates = LightUtil.getProperBlockLight(packedLight);
            float skyLightCoordinates = LightUtil.getProperSkyLight(packedLight);

            putVec3(buf, x, y, z);

            putVec2(buf, blockLightCoordinates, skyLightCoordinates);
            putFloat(buf, rotationalSpeed);
            putFloat(buf, rotationOffset);

            putVec3(buf, rotationAxisX, rotationAxisY, rotationAxisZ);
        }
    }
}
