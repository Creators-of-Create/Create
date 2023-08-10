package com.simibubi.create.content.contraptions.actors.harvester;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.content.contraptions.actors.AttachedActorBlock;
import com.simibubi.create.foundation.block.IBE;

import net.minecraft.world.level.block.entity.BlockEntityType;

public class HarvesterBlock extends AttachedActorBlock implements IBE<HarvesterBlockEntity> {

	public HarvesterBlock(Properties p_i48377_1_) {
		super(p_i48377_1_);
	}

	@Override
	public Class<HarvesterBlockEntity> getBlockEntityClass() {
		return HarvesterBlockEntity.class;
	}

	@Override
	public BlockEntityType<? extends HarvesterBlockEntity> getBlockEntityType() {
		return AllBlockEntityTypes.HARVESTER.get();
	}
	
}
