package com.simibubi.create.content.contraptions.base.flwdata;

import com.jozufozu.flywheel.backend.gl.buffer.VecBuffer;
import com.mojang.math.Vector3f;

import net.minecraft.core.Direction;

public class RotatingData extends KineticData {
    byte rotationAxisX;
    byte rotationAxisY;
    byte rotationAxisZ;

    public RotatingData setRotationAxis(Direction.Axis axis) {
        Direction orientation = Direction.get(Direction.AxisDirection.POSITIVE, axis);
        return setRotationAxis(orientation.step());
    }

    public RotatingData setRotationAxis(Vector3f axis) {
        return setRotationAxis(axis.x(), axis.y(), axis.z());
	}

	public RotatingData setRotationAxis(float rotationAxisX, float rotationAxisY, float rotationAxisZ) {
		this.rotationAxisX = (byte) (rotationAxisX * 127);
		this.rotationAxisY = (byte) (rotationAxisY * 127);
		this.rotationAxisZ = (byte) (rotationAxisZ * 127);
		markDirty();
		return this;
	}

	@Override
	public void write(VecBuffer buf) {
		super.write(buf);

		buf.putVec3(rotationAxisX, rotationAxisY, rotationAxisZ);
	}
}
