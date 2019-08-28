package com.simibubi.create.modules.contraptions.receivers;

import com.simibubi.create.foundation.utility.ItemDescription;
import com.simibubi.create.modules.contraptions.relays.EncasedShaftBlock;

import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.world.IBlockReader;

public class EncasedFanBlock extends EncasedShaftBlock {

	@Override
	public ItemDescription getDescription() {
		return new ItemDescription(color)
				.withSummary("Exchange rotational power for air flow and back.").createTabs();
	}
	
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new EncasedFanTileEntity();
	}
	
	@Override
	public BlockRenderLayer getRenderLayer() {
		return BlockRenderLayer.CUTOUT;
	}
	
}
