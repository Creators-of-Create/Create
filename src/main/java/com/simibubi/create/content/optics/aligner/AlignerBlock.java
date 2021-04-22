package com.simibubi.create.content.optics.aligner;

import javax.annotation.Nullable;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.wrench.IWrenchable;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.block.ProperDirectionalBlock;

import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.world.IBlockReader;

public class AlignerBlock extends ProperDirectionalBlock implements IWrenchable, ITE<AlignerTileEntity> {
	public AlignerBlock(Properties properties) {
		super(properties);
		setDefaultState(getDefaultState().with(FACING, Direction.DOWN));
	}

	@Override
	public Class<AlignerTileEntity> getTileEntityClass() {
		return AlignerTileEntity.class;
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Nullable
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return AllTileEntities.ALIGNER.create();
	}
}
