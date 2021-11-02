package com.simibubi.create.content.logistics.block.inventories;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.foundation.block.ITE;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class CreativeCrateBlock extends CrateBlock implements ITE<CreativeCrateTileEntity> {

	public CreativeCrateBlock(Properties p_i48415_1_) {
		super(p_i48415_1_);
	}

	@Override
	public void onPlace(BlockState state, Level worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
		withTileEntityDo(worldIn, pos, CreativeCrateTileEntity::onPlaced);
	}

	@Override
	public Class<CreativeCrateTileEntity> getTileEntityClass() {
		return CreativeCrateTileEntity.class;
	}

	@Override
	public BlockEntityType<? extends CreativeCrateTileEntity> getTileEntityType() {
		return AllTileEntities.CREATIVE_CRATE.get();
	}
}
