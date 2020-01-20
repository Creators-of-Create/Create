package com.simibubi.create.modules.logistics.block.transposer;

import com.simibubi.create.AllBlocks;

import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IBlockReader;

public class LinkedTransposerBlock extends TransposerBlock {

	@Override
	public BlockRenderLayer getRenderLayer() {
		return BlockRenderLayer.CUTOUT;
	}

	@Override
	protected BlockState getVerticalDefaultState() {
		return AllBlocks.VERTICAL_LINKED_TRANSPOSER.get().getDefaultState();
	}
	
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new LinkedTransposerTileEntity();
	}

	@Override
	protected boolean reactsToRedstone() {
		return false;
	}

	public static class Vertical extends LinkedTransposerBlock {
		@Override
		protected boolean isVertical() {
			return true;
		}
		
		@Override
		public ResourceLocation getLootTable() {
			return AllBlocks.LINKED_TRANSPOSER.get().getLootTable();
		}
	}

}
