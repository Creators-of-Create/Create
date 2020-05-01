package com.simibubi.create.foundation.gui.widgets;

import com.simibubi.create.foundation.utility.AngleHelper;

public class InterpolatedChasingAngle extends InterpolatedChasingValue {

	@Override
	protected float getCurrentDiff() {
		return AngleHelper.getShortestAngleDiff(value, getTarget());
	}

}
