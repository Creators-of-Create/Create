package com.simibubi.create.content.trains.station;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.block.ProperWaterloggedBlock;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class StationBlock extends Block implements IBE<StationBlockEntity>, IWrenchable, ProperWaterloggedBlock {
	public StationBlock(Properties properties) {
		super(properties);
	}

	@Override
	public Class<StationBlockEntity> getBlockEntityClass() {
		return StationBlockEntity.class;
	}

	@Override
	public BlockEntityType<? extends StationBlockEntity> getBlockEntityType() {
		return AllBlockEntityTypes.TRACK_STATION.get();
	}
}
