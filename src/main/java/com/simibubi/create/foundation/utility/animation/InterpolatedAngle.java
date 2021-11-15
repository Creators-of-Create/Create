package com.simibubi.create.foundation.utility.animation;

import com.simibubi.create.foundation.utility.AngleHelper;

/**
 * Use {@link LerpedFloat} instead.
 */
@Deprecated
public class InterpolatedAngle extends InterpolatedValue {
	
	public float get(float partialTicks) {
		return AngleHelper.angleLerp(partialTicks, lastValue, value);
	}

}
