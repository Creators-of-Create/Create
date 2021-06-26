package com.simibubi.create.content.curiosities.bell;

import com.simibubi.create.AllTileEntities;

import com.simibubi.create.foundation.block.ITE;

import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

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
