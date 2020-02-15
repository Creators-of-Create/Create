package com.simibubi.create.modules.contraptions.components.contraptions.bearing;

import com.simibubi.create.modules.contraptions.components.contraptions.IControlContraption;

public interface IBearingTileEntity extends IControlContraption {

	float getInterpolatedAngle(float partialTicks);

}
