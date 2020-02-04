package com.simibubi.create.modules.contraptions.components.motor;

import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.foundation.utility.SuperByteBuffer;
import com.simibubi.create.modules.contraptions.base.KineticTileEntity;
import com.simibubi.create.modules.contraptions.base.KineticTileEntityRenderer;

public class MotorTileEntityRenderer extends KineticTileEntityRenderer {

	@Override
	protected SuperByteBuffer getRotatedModel(KineticTileEntity te) {
		return AllBlockPartials.SHAFT_HALF.renderOnHorizontal(te.getBlockState());
	}

}
