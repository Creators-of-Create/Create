package com.simibubi.create.modules.contraptions.relays.encased;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.modules.contraptions.base.KineticTileEntity;
import com.simibubi.create.modules.contraptions.base.KineticTileEntityRenderer;

import net.minecraft.block.BlockState;
import net.minecraft.state.properties.BlockStateProperties;

public class EncasedShaftTileEntityRenderer extends KineticTileEntityRenderer {

	@Override
	protected BlockState getRenderedBlockState(KineticTileEntity te) {
		return AllBlocks.SHAFT.get().getDefaultState().with(BlockStateProperties.AXIS,
				te.getBlockState().get(BlockStateProperties.AXIS));
	}

}
