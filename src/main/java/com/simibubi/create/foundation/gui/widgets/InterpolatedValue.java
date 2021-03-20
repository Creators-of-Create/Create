package com.simibubi.create.foundation.gui.widgets;

import net.minecraft.util.math.MathHelper;

public class InterpolatedValue {

	public float value = 0;
	public float lastValue = 0;
	
	public InterpolatedValue set(float value) {
		lastValue = this.value;
		this.value = value;
		return this;
	}
	
	public float get(float partialTicks) {
		return MathHelper.lerp(partialTicks, lastValue, value);
	}

	public boolean settled() {
		return Math.abs(value - lastValue) < 1e-3;
	}
	
}
