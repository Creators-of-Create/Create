package com.simibubi.create.content.curiosities.bell;

import com.jozufozu.flywheel.core.PartialModel;
import com.simibubi.create.AllBlockPartials;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class PeculiarBellTileEntity extends AbstractBellTileEntity {

	public PeculiarBellTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public PartialModel getBellModel() {
		return AllBlockPartials.PECULIAR_BELL;
	}

}
