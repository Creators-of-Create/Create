package com.simibubi.create.content.equipment.goggles;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface IProxyHoveringInformation {
	
	public BlockPos getInformationSource(Level level, BlockPos pos, BlockState state);

}
