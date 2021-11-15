package com.simibubi.create.foundation.utility.animation;

import com.simibubi.create.foundation.utility.AngleHelper;

/**
 * Use {@link LerpedFloat} instead.
 */
@Deprecated
public class InterpolatedChasingAngle extends InterpolatedChasingValue {

	public float get(float partialTicks) {
		return AngleHelper.angleLerp(partialTicks, lastValue, value);
	}
	
	@Override
	protected float getCurrentDiff() {
		return AngleHelper.getShortestAngleDiff(value, getTarget());
	}

}
