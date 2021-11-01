package com.simibubi.create.foundation.gui.widgets;

import net.minecraft.util.Mth;

public class InterpolatedValue {

	public float value = 0;
	public float lastValue = 0;
	
	public InterpolatedValue set(float value) {
		lastValue = this.value;
		this.value = value;
		return this;
	}

	public InterpolatedValue init(float value) {
		this.lastValue = this.value = value;
		return this;
	}
	
	public float get(float partialTicks) {
		return Mth.lerp(partialTicks, lastValue, value);
	}

	public boolean settled() {
		return Math.abs(value - lastValue) < 1e-3;
	}
	
}
