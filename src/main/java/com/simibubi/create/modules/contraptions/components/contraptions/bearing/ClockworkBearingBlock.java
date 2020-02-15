package com.simibubi.create.modules.contraptions.components.contraptions.bearing;

import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;

public class ClockworkBearingBlock extends BearingBlock {

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new ClockworkBearingTileEntity();
	}

}
