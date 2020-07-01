package com.simibubi.create.foundation.gui.widgets;

import com.simibubi.create.foundation.utility.AngleHelper;

public class InterpolatedAngle extends InterpolatedValue {
	
	public float get(float partialTicks) {
		return AngleHelper.angleLerp(partialTicks, lastValue, value);
	}

}
