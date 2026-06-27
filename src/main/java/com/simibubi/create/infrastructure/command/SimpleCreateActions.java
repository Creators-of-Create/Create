package com.simibubi.create.infrastructure.command;

import com.simibubi.create.Create;
import com.simibubi.create.foundation.utility.CameraAngleAnimationService;

public class SimpleCreateActions {
	public static void camAngleTarget(String value, boolean yaw) {
		try {
			float v = Float.parseFloat(value);

			if (yaw) {
				CameraAngleAnimationService.setYawTarget(v);
			} else {
				CameraAngleAnimationService.setPitchTarget(v);
			}

		} catch (NumberFormatException ignored) {
			Create.LOGGER.debug("Received non-float value {} in camAngle packet, ignoring", value);
		}
	}
}
