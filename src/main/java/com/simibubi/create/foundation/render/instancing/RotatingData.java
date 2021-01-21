package com.simibubi.create.foundation.render.instancing;

import net.minecraft.client.renderer.Vector3f;

import java.nio.ByteBuffer;

import static com.simibubi.create.foundation.render.instancing.VertexAttribute.*;

public class RotatingData extends BasicData<RotatingData> {
    public static VertexFormat FORMAT = new VertexFormat(BasicData.FORMAT, FLOAT, FLOAT, NORMAL);

    private float rotationalSpeed;
    private float rotationOffset;
    private byte rotationAxisX;
    private byte rotationAxisY;
    private byte rotationAxisZ;

    public RotatingData setRotationalSpeed(float rotationalSpeed) {
        this.rotationalSpeed = rotationalSpeed;
        return this;
    }

    public RotatingData setRotationOffset(float rotationOffset) {
        this.rotationOffset = rotationOffset;
        return this;
    }

    public RotatingData setRotationAxis(Vector3f axis) {
        setRotationAxis(axis.getX(), axis.getY(), axis.getZ());
        return this;
    }

    public RotatingData setRotationAxis(float rotationAxisX, float rotationAxisY, float rotationAxisZ) {
        this.rotationAxisX = (byte) (rotationAxisX * 127);
        this.rotationAxisY = (byte) (rotationAxisY * 127);
        this.rotationAxisZ = (byte) (rotationAxisZ * 127);
        return this;
    }

    @Override
    public void write(ByteBuffer buf) {
        super.write(buf);
        put(buf, rotationalSpeed);
        put(buf, rotationOffset);

        putVec3(buf, rotationAxisX, rotationAxisY, rotationAxisZ);
    }
}
