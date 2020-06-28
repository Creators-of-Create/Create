package com.simibubi.create.content.contraptions.relays.belt.transport;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.relays.belt.BeltHelper;
import com.simibubi.create.content.logistics.block.realityFunnel.BeltFunnelBlock;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.inventory.InsertingBehaviour;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class BeltFunnelInteractionHandler {

	public static boolean checkForFunnels(BeltInventory beltInventory, TransportedItemStack currentItem,
		float nextOffset) {
		boolean beltMovementPositive = beltInventory.beltMovementPositive;
		int firstUpcomingSegment = (int) (currentItem.beltPosition + (beltMovementPositive ? .5f : -.5f));
		int step = beltMovementPositive ? 1 : -1;
		firstUpcomingSegment = MathHelper.clamp(firstUpcomingSegment, 0, beltInventory.belt.beltLength - 1);

		for (int segment = firstUpcomingSegment; beltMovementPositive ? segment + .5f <= nextOffset
			: segment + .5f >= nextOffset; segment += step) {
			BlockPos funnelPos = BeltHelper.getPositionForOffset(beltInventory.belt, segment)
				.up();
			World world = beltInventory.belt.getWorld();
			BlockState funnelState = world.getBlockState(funnelPos);
			if (!AllBlocks.BELT_FUNNEL.has(funnelState))
				continue;
			if (funnelState.get(BeltFunnelBlock.HORIZONTAL_FACING) != beltInventory.belt.getMovementFacing()
				.getOpposite())
				continue;

			currentItem.beltPosition = segment + .5f;

			if (funnelState.get(BeltFunnelBlock.PUSHING))
				return true;
			if (funnelState.get(BeltFunnelBlock.POWERED))
				return true;
			InsertingBehaviour behaviour = TileEntityBehaviour.get(world, funnelPos, InsertingBehaviour.TYPE);
			FilteringBehaviour filtering = TileEntityBehaviour.get(world, funnelPos, FilteringBehaviour.TYPE);
			if (behaviour == null || world.isRemote)
				return true;
			if (filtering != null && !filtering.test(currentItem.stack))
				return true;

			ItemStack before = currentItem.stack.copy();
			ItemStack remainder = behaviour.insert(before, false);
			if (before.equals(remainder, false))
				return true;

			currentItem.stack = remainder;
			beltInventory.belt.sendData();
			return true;
		}

		return false;
	}

}
