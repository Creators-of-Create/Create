package com.simibubi.create.content.contraptions.components.actors.dispenser;

import net.minecraft.dispenser.IPosition;

public class SimplePos implements IPosition {
	private final double x;
	private final double y;
	private final double z;

	public SimplePos(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	@Override
	public double x() {
		return x;
	}

	@Override
	public double y() {
		return y;
	}

	@Override
	public double z() {
		return z;
	}
}
