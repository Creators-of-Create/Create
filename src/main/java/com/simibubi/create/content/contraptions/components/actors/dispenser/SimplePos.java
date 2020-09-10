package com.simibubi.create.content.contraptions.components.actors.dispenser;

import net.minecraft.dispenser.IPosition;

public class SimplePos implements IPosition {
	private final int x;
	private final int y;
	private final int z;

	public SimplePos(double x, double y, double z) {
		this.x = (int) Math.round(x);
		this.y = (int) Math.round(y);
		this.z = (int) Math.round(z);
	}

	@Override
	public double getX() {
		return x;
	}

	@Override
	public double getY() {
		return y;
	}

	@Override
	public double getZ() {
		return z;
	}
}
