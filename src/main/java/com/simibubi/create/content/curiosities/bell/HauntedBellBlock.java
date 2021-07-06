package com.simibubi.create.content.curiosities.bell;

import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.AllTileEntities;

import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class HauntedBellBlock extends AbstractBellBlock<HauntedBellTileEntity> {

	public HauntedBellBlock(Properties properties) {
		super(properties);
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return AllTileEntities.HAUNTED_BELL.create();
	}

	@Override
	public Class<HauntedBellTileEntity> getTileEntityClass() {
		return HauntedBellTileEntity.class;
	}

	@Override
	public void playSound(World world, BlockPos pos) {
		AllSoundEvents.HAUNTED_BELL_USE.playOnServer(world, pos, 4f, 1f);
	}

	@Override
	public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean isMoving) {
		if (oldState.getBlock() != this)
			withTileEntityDo(world, pos, HauntedBellTileEntity::startEffect);
	}

}
