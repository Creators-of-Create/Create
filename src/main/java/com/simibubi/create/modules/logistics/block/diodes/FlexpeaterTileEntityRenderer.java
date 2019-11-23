package com.simibubi.create.modules.logistics.block.diodes;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.block.ColoredOverlayTileEntityRenderer;
import com.simibubi.create.foundation.utility.ColorHelper;

import net.minecraft.block.BlockState;

public class FlexpeaterTileEntityRenderer extends ColoredOverlayTileEntityRenderer<FlexpeaterTileEntity> {

	@Override
	protected int getColor(FlexpeaterTileEntity te, float partialTicks) {
		return ColorHelper.mixColors(0x2C0300, 0xCD0000, te.state / (float) te.maxState);
	}

	@Override
	protected BlockState getOverlayState(FlexpeaterTileEntity te) {
		return AllBlocks.FLEXPEATER_INDICATOR.get().getDefaultState();
	}

}
