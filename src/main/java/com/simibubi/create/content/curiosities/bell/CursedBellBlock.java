package com.simibubi.create.content.curiosities.bell;

import com.simibubi.create.AllTileEntities;

import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;

public class CursedBellBlock extends AbstractBellBlock<CursedBellTileEntity> {

	public CursedBellBlock(Properties properties) {
		super(properties);
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return AllTileEntities.CURSED_BELL.create();
	}

	@Override
	public Class<CursedBellTileEntity> getTileEntityClass() {
		return CursedBellTileEntity.class;
	}

}
