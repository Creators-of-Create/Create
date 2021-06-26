package com.simibubi.create.content.curiosities.bell;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.foundation.block.ITE;

import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

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
