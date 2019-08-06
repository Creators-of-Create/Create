package com.simibubi.create.modules.kinetics.relays;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.modules.kinetics.base.KineticTileEntity;
import com.simibubi.create.modules.kinetics.base.KineticTileEntityRenderer;

import net.minecraft.block.BlockState;
import net.minecraft.state.properties.BlockStateProperties;

public class AxisTunnelTileEntityRenderer extends KineticTileEntityRenderer {

	@Override
	protected BlockState getRenderedBlockState(KineticTileEntity te) {
		return AllBlocks.AXIS.get().getDefaultState().with(BlockStateProperties.AXIS,
				te.getBlockState().get(BlockStateProperties.AXIS));
	}

}
