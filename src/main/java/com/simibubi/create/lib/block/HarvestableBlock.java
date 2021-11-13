package com.simibubi.create.lib.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

public interface HarvestableBlock {
	boolean canHarvestBlock(BlockState state, BlockGetter world, BlockPos pos, Player player);

	boolean isToolEffective(BlockState state, DiggerItem tool);
}
