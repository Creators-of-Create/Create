package com.simibubi.create.modules.kinetics.generators;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.modules.kinetics.base.KineticTileEntity;
import com.simibubi.create.modules.kinetics.base.KineticTileEntityRenderer;

import net.minecraft.block.BlockState;
import net.minecraft.state.properties.BlockStateProperties;

public class MotorTileEntityRenderer extends KineticTileEntityRenderer {

	@Override
	protected BlockState getRenderedBlockState(KineticTileEntity te) {
		return AllBlocks.HALF_AXIS.get().getDefaultState().with(BlockStateProperties.FACING,
				te.getBlockState().get(BlockStateProperties.HORIZONTAL_FACING));
	}

}
