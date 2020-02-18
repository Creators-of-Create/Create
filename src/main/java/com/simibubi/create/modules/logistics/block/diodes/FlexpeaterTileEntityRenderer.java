package com.simibubi.create.modules.logistics.block.diodes;

import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.foundation.block.render.ColoredOverlayTileEntityRenderer;
import com.simibubi.create.foundation.utility.ColorHelper;
import com.simibubi.create.foundation.utility.SuperByteBuffer;

public class FlexpeaterTileEntityRenderer extends ColoredOverlayTileEntityRenderer<FlexpeaterTileEntity> {

	@Override
	protected int getColor(FlexpeaterTileEntity te, float partialTicks) {
		return ColorHelper.mixColors(0x2C0300, 0xCD0000, te.state / (float) te.maxState.getValue());
	}

	@Override
	protected SuperByteBuffer getOverlayBuffer(FlexpeaterTileEntity te) {
		return AllBlockPartials.FLEXPEATER_INDICATOR.renderOn(te.getBlockState());
	}

}
