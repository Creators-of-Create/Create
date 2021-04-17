package com.simibubi.create.content.optics;

import javax.annotation.Nonnull;

import net.minecraft.util.math.vector.Vector3d;

public class BeamSegment {
	public final float[] colors;
	private final Vector3d direction;
	private final Vector3d start;
	private int length;

	public BeamSegment(@Nonnull float[] color, Vector3d start, Vector3d direction) {
		this.colors = color;
		this.direction = direction;
		this.start = start;
		this.length = 1;
	}

	public void incrementLength() {
		++this.length;
	}

	public float[] getColors() {
		return this.colors;
	}

	public int getLength() {
		return this.length;
	}

	public Vector3d getDirection() {
		return direction;
	}

	public Vector3d getStart() {
		return start;
	}
}
