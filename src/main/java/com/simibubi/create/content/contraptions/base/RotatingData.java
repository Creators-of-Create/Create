package com.simibubi.create.content.contraptions.base;

import java.nio.ByteBuffer;

import com.simibubi.create.foundation.render.backend.gl.attrib.VertexFormat;
import com.simibubi.create.foundation.render.backend.instancing.InstancedModel;

import net.minecraft.client.renderer.Vector3f;
import net.minecraft.util.Direction;

public class RotatingData extends KineticData<RotatingData> {
    public static VertexFormat FORMAT = VertexFormat.builder()
                                                    .addAttributes(KineticVertexAttributes.class)
                                                    .addAttributes(RotatingVertexAttributes.class)
                                                    .build();

    private byte rotationAxisX;
    private byte rotationAxisY;
    private byte rotationAxisZ;

    protected RotatingData(InstancedModel<?> owner) {
        super(owner);
    }

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
