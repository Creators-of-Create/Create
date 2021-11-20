package com.simibubi.create.lib.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface BlockUseBypassingItem {
	boolean shouldBypass(BlockState state, BlockPos pos, Level level, Player player, InteractionHand hand);
}
