package com.simibubi.create.content.curiosities.bell;

import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.AllTileEntities;

import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
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

	@Override
	public void playSound(World world, BlockPos pos) {
		AllSoundEvents.CURSED_BELL_USE.playOnServer(world, pos, 4f, 1f);
	}

	@Override
	public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean isMoving) {
		if (oldState.getBlock() != this)
			withTileEntityDo(world, pos, CursedBellTileEntity::startEffect);
	}

}
