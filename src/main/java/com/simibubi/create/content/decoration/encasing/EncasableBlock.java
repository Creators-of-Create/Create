package com.simibubi.create.content.decoration.encasing;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Implement this interface to indicate that this block is encasable.
 */
public interface EncasableBlock {
	/**
	 * This method should be called in the {@link Block#use(BlockState, Level, BlockPos, Player, InteractionHand, BlockHitResult)} method.
	 */
	default InteractionResult tryEncase(BlockState state, Level level, BlockPos pos, ItemStack heldItem, Player player, InteractionHand hand,
		BlockHitResult ray) {
		List<Block> encasedVariants = EncasingRegistry.getVariants(state.getBlock());
		for (Block block : encasedVariants) {
			if (block instanceof EncasedBlock encased) {
				if (encased.getCasing().asItem() != heldItem.getItem())
					continue;

				if (level.isClientSide)
					return InteractionResult.SUCCESS;

				encased.handleEncasing(state, level, pos, heldItem, player, hand, ray);
				return InteractionResult.SUCCESS;
			}
		}
		return InteractionResult.PASS;
	}
}
