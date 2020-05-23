package com.simibubi.create.content.contraptions.components.structureMovement.bearing;

import com.simibubi.create.content.contraptions.components.structureMovement.IControlContraption;

public interface IBearingTileEntity extends IControlContraption {

	float getInterpolatedAngle(float partialTicks);

}
