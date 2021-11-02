package com.simibubi.create.content.contraptions.components.actors;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.foundation.block.ITE;

import net.minecraft.world.level.block.entity.BlockEntityType;

public class HarvesterBlock extends AttachedActorBlock implements ITE<HarvesterTileEntity> {

	public HarvesterBlock(Properties p_i48377_1_) {
		super(p_i48377_1_);
	}

	@Override
	public Class<HarvesterTileEntity> getTileEntityClass() {
		return HarvesterTileEntity.class;
	}

	@Override
	public BlockEntityType<? extends HarvesterTileEntity> getTileEntityType() {
		return AllTileEntities.HARVESTER.get();
	}
	
}
