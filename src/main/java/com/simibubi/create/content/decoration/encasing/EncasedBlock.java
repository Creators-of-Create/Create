package com.simibubi.create.content.decoration.encasing;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Implement this interface to indicate that this block is encased.
 */
public interface EncasedBlock {
	Block getCasing();

	/**
	 * Handles how encasing should be done if {@link EncasableBlock#tryEncase(BlockState, Level, BlockPos, ItemStack, Player, InteractionHand, BlockHitResult)} is successful.
	 */
	default void handleEncasing(BlockState state, Level level, BlockPos pos, ItemStack heldItem, Player player, InteractionHand hand, BlockHitResult ray) {
	}
}
