package com.simibubi.create.foundation.block;

import java.util.HashSet;
import java.util.Set;

import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class ItemUseOverrides {

	private static final Set<ResourceLocation> OVERRIDES = new HashSet<>();

	public static void addBlock(Block block) {
		OVERRIDES.add(Registry.BLOCK.getKey(block));
	}

	public static InteractionResult onBlockActivated(Player player, Level world, InteractionHand hand, BlockHitResult traceResult) {
		if (AllItems.WRENCH.isIn(player.getItemInHand(hand)))
			return InteractionResult.PASS;

		if (player.isSpectator())
			return InteractionResult.PASS;

		BlockPos pos = traceResult.getBlockPos();

		BlockState state = world
				.getBlockState(pos);
		ResourceLocation id = Registry.BLOCK.getKey(state.getBlock());

		if (!OVERRIDES.contains(id))
			return InteractionResult.PASS;

		BlockHitResult blockTrace =
				new BlockHitResult(VecHelper.getCenterOf(pos), traceResult.getDirection(), pos, true);
		InteractionResult result = state.use(world, player, hand, blockTrace);

		if (!result.consumesAction())
			return InteractionResult.PASS;

		return result;
	}

}
