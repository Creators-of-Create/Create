package com.simibubi.create.content.contraptions.actors.flwdata;

import com.jozufozu.flywheel.api.InstanceData;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;

import net.minecraft.core.BlockPos;

public class ActorData extends InstanceData {
    float x;
    float y;
    float z;
    byte blockLight;
    byte skyLight;
    float rotationOffset;
    byte rotationAxisX;
    byte rotationAxisY;
    byte rotationAxisZ;
    float qX;
    float qY;
    float qZ;
    float qW;
    byte rotationCenterX = 64;
    byte rotationCenterY = 64;
    byte rotationCenterZ = 64;
    float speed;

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
        setRotationAxis(axis.x(), axis.y(), axis.z());
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
        setRotationCenter(axis.x(), axis.y(), axis.z());
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
		this.qX = q.i();
		this.qY = q.j();
		this.qZ = q.k();
		this.qW = q.r();
		markDirty();
		return this;
	}

}
