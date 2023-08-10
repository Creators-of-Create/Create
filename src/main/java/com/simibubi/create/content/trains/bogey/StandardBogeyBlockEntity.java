package com.simibubi.create.content.trains.bogey;

import com.simibubi.create.AllBogeyStyles;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class StandardBogeyBlockEntity extends AbstractBogeyBlockEntity {

	public StandardBogeyBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public BogeyStyle getDefaultStyle() {
		return AllBogeyStyles.STANDARD;
	}
}
