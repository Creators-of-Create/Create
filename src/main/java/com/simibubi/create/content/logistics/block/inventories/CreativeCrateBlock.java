package com.simibubi.create.content.logistics.block.inventories;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.foundation.block.ITE;

import net.minecraft.world.level.block.entity.BlockEntityType;

public class CreativeCrateBlock extends CrateBlock implements ITE<CreativeCrateTileEntity> {

	public CreativeCrateBlock(Properties p_i48415_1_) {
		super(p_i48415_1_);
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
