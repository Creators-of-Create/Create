package com.simibubi.create.foundation.render.instancing.actors;

import com.simibubi.create.foundation.render.instancing.InstanceData;
import com.simibubi.create.foundation.render.instancing.RotatingData;
import com.simibubi.create.foundation.render.instancing.VertexFormat;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.util.math.BlockPos;

import java.nio.ByteBuffer;

import static com.simibubi.create.foundation.render.instancing.VertexAttribute.*;

public class StaticRotatingActorData extends InstanceData {
    public static VertexFormat FORMAT = new VertexFormat(POSITION, FLOAT, NORMAL, NORMAL);

    private float x;
    private float y;
    private float z;
    private float rotationOffset;
    private byte rotationAxisX;
    private byte rotationAxisY;
    private byte rotationAxisZ;
    private byte localOrientationX;
    private byte localOrientationY;
    private byte localOrientationZ;

    public StaticRotatingActorData setPosition(BlockPos pos) {
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
        return this;
    }

    public StaticRotatingActorData setRotationOffset(float rotationOffset) {
        this.rotationOffset = rotationOffset;
        return this;
    }

    public StaticRotatingActorData setRotationAxis(Vector3f axis) {
        setRotationAxis(axis.getX(), axis.getY(), axis.getZ());
        return this;
    }

    public StaticRotatingActorData setRotationAxis(float rotationAxisX, float rotationAxisY, float rotationAxisZ) {
        this.rotationAxisX = (byte) (rotationAxisX * 127);
        this.rotationAxisY = (byte) (rotationAxisY * 127);
        this.rotationAxisZ = (byte) (rotationAxisZ * 127);
        return this;
    }

    public StaticRotatingActorData setLocalOrientation(Vector3f axis) {
        setRotationAxis(axis.getX(), axis.getY(), axis.getZ());
        return this;
    }

    public StaticRotatingActorData setLocalOrientation(float localOrientationX, float localOrientationY, float localOrientationZ) {
        this.localOrientationX = (byte) (localOrientationX * 127);
        this.localOrientationY = (byte) (localOrientationY * 127);
        this.localOrientationZ = (byte) (localOrientationZ * 127);
        return this;
    }

    @Override
    public void write(ByteBuffer buf) {
        putVec3(buf, x, y, z);
        put(buf, rotationOffset);
        putVec3(buf, rotationAxisX, rotationAxisY, rotationAxisZ);
        putVec3(buf, localOrientationX, localOrientationY, localOrientationZ);

    }
}
