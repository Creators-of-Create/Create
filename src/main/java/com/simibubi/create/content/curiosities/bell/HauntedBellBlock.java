package com.simibubi.create.content.curiosities.bell;

import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.AllTileEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class HauntedBellBlock extends AbstractBellBlock<HauntedBellTileEntity> {

	public HauntedBellBlock(Properties properties) {
		super(properties);
	}

	@Override
	public BlockEntityType<? extends HauntedBellTileEntity> getTileEntityType() {
		return AllTileEntities.HAUNTED_BELL.get();
	}
	
	@Override
	public Class<HauntedBellTileEntity> getTileEntityClass() {
		return HauntedBellTileEntity.class;
	}

	@Override
	public void playSound(Level world, BlockPos pos) {
		AllSoundEvents.HAUNTED_BELL_USE.playOnServer(world, pos, 4f, 1f);
	}

	@Override
	public void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean isMoving) {
		if (oldState.getBlock() != this)
			withTileEntityDo(world, pos, HauntedBellTileEntity::startEffect);
	}

}
