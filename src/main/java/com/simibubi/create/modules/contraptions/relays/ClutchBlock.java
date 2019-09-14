package com.simibubi.create.modules.contraptions.relays;

import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;

public class ClutchBlock extends GearshiftBlock {

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new ClutchTileEntity();
	}

}
