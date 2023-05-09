package com.simibubi.create.content.logistics.trains.track;

import com.simibubi.create.AllBogeyStyles;
import com.simibubi.create.content.logistics.trains.entity.BogeyStyle;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class StandardBogeyTileEntity extends AbstractBogeyTileEntity {
	public StandardBogeyTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public BogeyStyle getDefaultStyle() {
		return AllBogeyStyles.STANDARD;
	}
}
