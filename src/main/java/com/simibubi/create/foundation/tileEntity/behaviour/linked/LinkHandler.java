package com.simibubi.create.foundation.tileEntity.behaviour.linked;

import java.util.Arrays;

import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.utility.RaycastHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

public class LinkHandler {

	public static InteractionResult onBlockActivated(Player player, Level world, InteractionHand hand, BlockHitResult blockRayTraceResult) {
		BlockPos pos = blockRayTraceResult.getBlockPos();

		if (player.isShiftKeyDown() || player.isSpectator())
			return InteractionResult.PASS;

		LinkBehaviour behaviour = TileEntityBehaviour.get(world, pos, LinkBehaviour.TYPE);
		if (behaviour == null)
			return InteractionResult.PASS;

		ItemStack heldItem = player.getItemInHand(hand);
		BlockHitResult ray = RaycastHelper.rayTraceRange(world, player, 10);
		if (ray == null)
			return InteractionResult.PASS;
		if (AllItems.LINKED_CONTROLLER.isIn(heldItem))
			return InteractionResult.PASS;
		if (AllItems.WRENCH.isIn(heldItem))
			return InteractionResult.PASS;

		for (boolean first : Arrays.asList(false, true)) {
			if (behaviour.testHit(first, ray.getLocation())) {
				if (!world.isClientSide)
					behaviour.setFrequency(first, heldItem);
				world.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, .25f, .1f);
				return InteractionResult.SUCCESS;
			}
		}
		return InteractionResult.PASS;
	}

}
