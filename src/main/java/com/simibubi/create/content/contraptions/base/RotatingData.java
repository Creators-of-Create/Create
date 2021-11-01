package com.simibubi.create.content.contraptions.base;

import com.jozufozu.flywheel.backend.gl.buffer.VecBuffer;

import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3f;

public class RotatingData extends KineticData {
    private byte rotationAxisX;
    private byte rotationAxisY;
    private byte rotationAxisZ;

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
