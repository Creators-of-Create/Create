package com.simibubi.create.content.contraptions.components.actors;

import com.jozufozu.flywheel.backend.gl.buffer.MappedBuffer;
import com.jozufozu.flywheel.backend.instancing.GPUInstancer;
import com.jozufozu.flywheel.backend.instancing.InstanceData;
import com.jozufozu.flywheel.backend.instancing.Instancer;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;

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

    public ActorData(Instancer<?> owner) {
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

	@Override
	public void write(MappedBuffer buf) {
		buf.putVec3(x, y, z);
		buf.putVec2(blockLight, skyLight);
		buf.putFloat(rotationOffset);
		buf.putVec3(rotationAxisX, rotationAxisY, rotationAxisZ);
		buf.putVec4(qX, qY, qZ, qW);
		buf.putVec3(rotationCenterX, rotationCenterY, rotationCenterZ);
		buf.putFloat(speed);

	}
}
