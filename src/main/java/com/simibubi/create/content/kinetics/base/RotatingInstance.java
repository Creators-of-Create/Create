package com.simibubi.create.content.kinetics.base;

import org.joml.Vector3f;

import dev.engine_room.flywheel.api.instance.InstanceHandle;
import dev.engine_room.flywheel.api.instance.InstanceType;

import net.minecraft.core.Direction;

public class RotatingInstance extends KineticInstance {
    public byte rotationAxisX;
    public byte rotationAxisY;
    public byte rotationAxisZ;

	public RotatingInstance(InstanceType<? extends KineticInstance> type, InstanceHandle handle) {
		super(type, handle);
	}

	public RotatingInstance setRotationAxis(Direction.Axis axis) {
        Direction orientation = Direction.get(Direction.AxisDirection.POSITIVE, axis);
        return setRotationAxis(orientation.step());
    }

    public RotatingInstance setRotationAxis(Vector3f axis) {
        return setRotationAxis(axis.x(), axis.y(), axis.z());
	}

	public RotatingInstance setRotationAxis(float rotationAxisX, float rotationAxisY, float rotationAxisZ) {
		this.rotationAxisX = (byte) (rotationAxisX * 127);
		this.rotationAxisY = (byte) (rotationAxisY * 127);
		this.rotationAxisZ = (byte) (rotationAxisZ * 127);
		return this;
	}

}
