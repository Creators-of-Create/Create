package com.simibubi.create.content.contraptions.actors;

import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;

import dev.engine_room.flywheel.api.instance.InstanceHandle;
import dev.engine_room.flywheel.api.instance.InstanceType;
import dev.engine_room.flywheel.lib.instance.AbstractInstance;

import net.minecraft.core.BlockPos;

public class ActorInstance extends AbstractInstance {
	public float x;
	public float y;
	public float z;
	public byte blockLight;
	public byte skyLight;
	public float rotationOffset;
	public byte rotationAxisX;
	public byte rotationAxisY;
	public byte rotationAxisZ;
	public Quaternionf rotation = new Quaternionf();
	public byte rotationCenterX = 64;
	public byte rotationCenterY = 64;
	public byte rotationCenterZ = 64;
	public float speed;

	public ActorInstance(InstanceType<?> type, InstanceHandle handle) {
		super(type, handle);
	}

	public ActorInstance setPosition(BlockPos pos) {
		this.x = pos.getX();
		this.y = pos.getY();
		this.z = pos.getZ();
		return this;
	}

	public ActorInstance setBlockLight(int blockLight) {
		this.blockLight = (byte) blockLight;
		return this;
	}

	public ActorInstance setSkyLight(int skyLight) {
		this.skyLight = (byte) skyLight;
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

	public ActorInstance setLocalRotation(Quaternionfc q) {
		this.rotation.set(q);
		return this;
	}

}
