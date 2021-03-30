package com.simibubi.create.content.contraptions.components.actors;

import com.simibubi.create.foundation.render.backend.instancing.InstanceData;
import com.simibubi.create.foundation.render.backend.instancing.InstancedModel;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;

import java.nio.ByteBuffer;

public class ActorData extends InstanceData {
    private float x;
    private float y;
    private float z;
    private byte blockLight;
    private byte skyLight;
    private float rotationOffset;
    private byte rotationAxisX;
    private byte rotationAxisY;
    private byte rotationAxisZ;
    private float qX;
    private float qY;
    private float qZ;
    private float qW;
    private byte rotationCenterX = 64;
    private byte rotationCenterY = 64;
    private byte rotationCenterZ = 64;

    private float speed;

    protected ActorData(InstancedModel<?> owner) {
        super(owner);
    }


    public ActorData setPosition(BlockPos pos) {
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
        markDirty();
        return this;
    }

    public ActorData setBlockLight(int blockLight) {
        this.blockLight = (byte) ((blockLight & 0xF) << 4);
        markDirty();
        return this;
    }

    public ActorData setSkyLight(int skyLight) {
        this.skyLight = (byte) ((skyLight & 0xF) << 4);
        markDirty();
        return this;
    }

    public ActorData setRotationOffset(float rotationOffset) {
        this.rotationOffset = rotationOffset;
        markDirty();
        return this;
    }

    public ActorData setSpeed(float speed) {
        this.speed = speed;
        markDirty();
        return this;
    }

    public ActorData setRotationAxis(Vector3f axis) {
        setRotationAxis(axis.getX(), axis.getY(), axis.getZ());
        return this;
    }

    public ActorData setRotationAxis(float rotationAxisX, float rotationAxisY, float rotationAxisZ) {
        this.rotationAxisX = (byte) (rotationAxisX * 127);
        this.rotationAxisY = (byte) (rotationAxisY * 127);
        this.rotationAxisZ = (byte) (rotationAxisZ * 127);
        markDirty();
        return this;
    }

    public ActorData setRotationCenter(Vector3f axis) {
        setRotationCenter(axis.getX(), axis.getY(), axis.getZ());
        return this;
    }

    public ActorData setRotationCenter(float rotationCenterX, float rotationCenterY, float rotationCenterZ) {
        this.rotationCenterX = (byte) (rotationCenterX * 127);
        this.rotationCenterY = (byte) (rotationCenterY * 127);
        this.rotationCenterZ = (byte) (rotationCenterZ * 127);
        markDirty();
        return this;
    }

    public ActorData setLocalRotation(Quaternion q) {
        this.qX = q.getX();
        this.qY = q.getY();
        this.qZ = q.getZ();
        this.qW = q.getW();
        markDirty();
        return this;
    }

    @Override
    public void write(ByteBuffer buf) {
        putVec3(buf, x, y, z);
        putVec2(buf, blockLight, skyLight);
        put(buf, rotationOffset);
        putVec3(buf, rotationAxisX, rotationAxisY, rotationAxisZ);
        putVec4(buf, qX, qY, qZ, qW);
        putVec3(buf, rotationCenterX, rotationCenterY, rotationCenterZ);
        put(buf, speed);

    }
}
