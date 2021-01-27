package com.simibubi.create.foundation.render.instancing;

import net.minecraft.client.renderer.Vector3f;
import net.minecraft.util.Direction;

import java.nio.ByteBuffer;

import static com.simibubi.create.foundation.render.instancing.VertexAttribute.NORMAL;

public class RotatingData extends KineticData<RotatingData> {
    public static VertexFormat FORMAT = new VertexFormat(KineticData.FORMAT, NORMAL);

    private byte rotationAxisX;
    private byte rotationAxisY;
    private byte rotationAxisZ;

    public RotatingData setRotationAxis(Direction.Axis axis) {
        Direction orientation = Direction.getFacingFromAxis(Direction.AxisDirection.POSITIVE, axis);
        setRotationAxis(orientation.getUnitVector());
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

        putVec3(buf, rotationAxisX, rotationAxisY, rotationAxisZ);
    }
}
