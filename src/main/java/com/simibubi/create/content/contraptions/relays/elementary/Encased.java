package com.simibubi.create.content.contraptions.relays.elementary;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Implement this interface to indicate that this block is an EncasedBlock
 */
public interface Encased {

	Block getCasing();

	void setCasing(Block casing);

	/**
	 * Handles how encasement should be done if tryEncase is successful
	 */
	default void handleEncasing(BlockState state, Level level, BlockPos pos, Block encasedBlock, InteractionHand hand, ItemStack heldItem, Player player,
			BlockHitResult ray) {}
}
