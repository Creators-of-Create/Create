package com.simibubi.create.content.logistics.block.transposer;

import com.simibubi.create.AllBlocks;

import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;

public class LinkedTransposerBlock extends TransposerBlock {

	public LinkedTransposerBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected BlockState getVerticalDefaultState() {
		return AllBlocks.VERTICAL_LINKED_TRANSPOSER.getDefaultState();
	}
	
	@Override
	protected BlockState getHorizontalDefaultState() {
		return AllBlocks.LINKED_TRANSPOSER.getDefaultState();
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
		public Vertical(Properties properties) {
			super(properties);
		}

		@Override
		protected boolean isVertical() {
			return true;
		}
	}

}
