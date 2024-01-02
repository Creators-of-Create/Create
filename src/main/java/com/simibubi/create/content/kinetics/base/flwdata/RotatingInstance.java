package com.simibubi.create.content.kinetics.base.flwdata;

import org.joml.Vector3f;

import com.jozufozu.flywheel.api.instance.InstanceHandle;
import com.jozufozu.flywheel.api.instance.InstanceType;

import net.minecraft.core.Direction;

public class RotatingInstance extends KineticInstance {
    byte rotationAxisX;
    byte rotationAxisY;
    byte rotationAxisZ;

	protected RotatingInstance(InstanceType<? extends KineticInstance> type, InstanceHandle handle) {
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
