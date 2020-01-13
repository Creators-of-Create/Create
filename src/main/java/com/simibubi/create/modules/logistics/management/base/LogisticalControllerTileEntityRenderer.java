package com.simibubi.create.modules.logistics.management.base;

import static com.simibubi.create.modules.logistics.management.base.LogisticalControllerBlock.TYPE;
import static net.minecraft.state.properties.BlockStateProperties.FACING;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.block.render.ColoredOverlayTileEntityRenderer;

import net.minecraft.block.BlockState;

public class LogisticalControllerTileEntityRenderer
		extends ColoredOverlayTileEntityRenderer<LogisticalActorTileEntity> {

	@Override
	protected int getColor(LogisticalActorTileEntity te, float partialTicks) {
		return te.getColor();
	}

	@Override
	protected BlockState getOverlayState(LogisticalActorTileEntity te) {
		BlockState state = te.getBlockState();
		return AllBlocks.LOGISTICAL_CONTROLLER_INDICATOR.get().getDefaultState().with(FACING, state.get(FACING))
				.with(TYPE, state.get(TYPE));
	}

}
