package com.simibubi.create.content.contraptions.actors.flwdata;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import com.jozufozu.flywheel.api.instance.InstanceHandle;
import com.jozufozu.flywheel.api.instance.InstanceType;
import com.jozufozu.flywheel.lib.instance.AbstractInstance;

import net.minecraft.core.BlockPos;

public class ActorInstance extends AbstractInstance {
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

	protected ActorInstance(InstanceType<?> type, InstanceHandle handle) {
		super(type, handle);
	}

	public ActorInstance setPosition(BlockPos pos) {
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
        return this;
    }

    public ActorInstance setBlockLight(int blockLight) {
        this.blockLight = (byte) ((blockLight & 0xF) << 4);
        return this;
    }

    public ActorInstance setSkyLight(int skyLight) {
        this.skyLight = (byte) ((skyLight & 0xF) << 4);
        return this;
    }

    public ActorInstance setRotationOffset(float rotationOffset) {
        this.rotationOffset = rotationOffset;
        return this;
    }

    public ActorInstance setSpeed(float speed) {
        this.speed = speed;
        return this;
    }

    public ActorInstance setRotationAxis(Vector3f axis) {
        setRotationAxis(axis.x(), axis.y(), axis.z());
        return this;
    }

    public ActorInstance setRotationAxis(float rotationAxisX, float rotationAxisY, float rotationAxisZ) {
        this.rotationAxisX = (byte) (rotationAxisX * 127);
        this.rotationAxisY = (byte) (rotationAxisY * 127);
        this.rotationAxisZ = (byte) (rotationAxisZ * 127);
        return this;
    }

    public ActorInstance setRotationCenter(Vector3f axis) {
        setRotationCenter(axis.x(), axis.y(), axis.z());
        return this;
    }

    public ActorInstance setRotationCenter(float rotationCenterX, float rotationCenterY, float rotationCenterZ) {
        this.rotationCenterX = (byte) (rotationCenterX * 127);
        this.rotationCenterY = (byte) (rotationCenterY * 127);
        this.rotationCenterZ = (byte) (rotationCenterZ * 127);
        return this;
    }

	public ActorInstance setLocalRotation(Quaternionf q) {
		this.qX = q.x();
		this.qY = q.y();
		this.qZ = q.z();
		this.qW = q.w();
		return this;
	}

}
