package com.simibubi.create.foundation.gui.widgets;

public class InterpolatedChasingValue extends InterpolatedValue {

	float speed = 0.5f;
	float target = 0;
	float eps = 1 / 4096f;

	public void tick() {
		float diff = target - value;
		if (Math.abs(diff) < eps)
			return;
		set(value + (diff) * speed);
	}
	
	public InterpolatedChasingValue withSpeed(float speed) {
		this.speed = speed;
		return this;
	}

	public InterpolatedChasingValue target(float target) {
		this.target = target;
		return this;
	}

}
