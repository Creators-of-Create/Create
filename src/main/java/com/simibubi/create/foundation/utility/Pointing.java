package com.simibubi.create.foundation.utility;

import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.IStringSerializable;

public enum Pointing implements IStringSerializable {
	UP(0), LEFT(270), DOWN(180), RIGHT(90);

	private int xRotation;

	private Pointing(int xRotation) {
		this.xRotation = xRotation;
	}

	@Override
	public String getString() {
		return Lang.asId(name());
	}

	public int getXRotation() {
		return xRotation;
	}

	public Direction getCombinedDirection(Direction direction) {
		Axis axis = direction.getAxis();
		Direction top = axis == Axis.Y ? Direction.SOUTH : Direction.UP;
		int rotations = direction.getAxisDirection() == AxisDirection.NEGATIVE ? 4 - ordinal() : ordinal();
		for (int i = 0; i < rotations; i++)
			top = DirectionHelper.rotateAround(top, axis);
		return top;
	}

}
