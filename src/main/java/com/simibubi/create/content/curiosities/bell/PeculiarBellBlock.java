package com.simibubi.create.content.curiosities.bell;

import com.simibubi.create.AllTileEntities;

import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;

public class PeculiarBellBlock extends AbstractBellBlock<PeculiarBellTileEntity> {

	public PeculiarBellBlock(Properties properties) {
		super(properties);
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return AllTileEntities.PECULIAR_BELL.create();
	}

	@Override
	public Class<PeculiarBellTileEntity> getTileEntityClass() { return PeculiarBellTileEntity.class; }

}
