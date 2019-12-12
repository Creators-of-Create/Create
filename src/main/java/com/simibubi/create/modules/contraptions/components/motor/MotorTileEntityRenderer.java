package com.simibubi.create.modules.contraptions.components.motor;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.modules.contraptions.base.KineticTileEntity;
import com.simibubi.create.modules.contraptions.base.KineticTileEntityRenderer;

import net.minecraft.block.BlockState;
import net.minecraft.state.properties.BlockStateProperties;

public class MotorTileEntityRenderer extends KineticTileEntityRenderer {

	@Override
	protected BlockState getRenderedBlockState(KineticTileEntity te) {
		return AllBlocks.SHAFT_HALF.get().getDefaultState().with(BlockStateProperties.FACING,
				te.getBlockState().get(BlockStateProperties.HORIZONTAL_FACING));
	}

}
