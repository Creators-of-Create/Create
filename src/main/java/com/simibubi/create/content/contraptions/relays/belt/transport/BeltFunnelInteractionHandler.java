package com.simibubi.create.content.contraptions.relays.belt.transport;

import com.simibubi.create.content.contraptions.relays.belt.BeltHelper;
import com.simibubi.create.content.logistics.block.funnel.BeltFunnelBlock;
import com.simibubi.create.content.logistics.block.funnel.FunnelTileEntity;
import com.simibubi.create.foundation.tileEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.inventory.InvManipulationBehaviour;
import com.simibubi.create.foundation.utility.BlockHelper;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
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
			if (!(funnelState.getBlock() instanceof BeltFunnelBlock))
				continue;
			Direction funnelFacing = funnelState.get(BeltFunnelBlock.HORIZONTAL_FACING);
			Direction movementFacing = beltInventory.belt.getMovementFacing();
			boolean blocking = funnelFacing == movementFacing.getOpposite();
			if (funnelFacing == movementFacing)
				continue;

			currentItem.beltPosition = segment + .5f;

			if (world.isRemote)
				return blocking;
			if (funnelState.get(BeltFunnelBlock.PUSHING))
				return blocking;
			if (BlockHelper.hasBlockStateProperty(funnelState, BeltFunnelBlock.POWERED) && funnelState.get(BeltFunnelBlock.POWERED))
				return blocking;

			TileEntity te = world.getTileEntity(funnelPos);
			if (!(te instanceof FunnelTileEntity))
				return true;

			FunnelTileEntity funnelTE = (FunnelTileEntity) te;
			InvManipulationBehaviour inserting = funnelTE.getBehaviour(InvManipulationBehaviour.TYPE);
			FilteringBehaviour filtering = funnelTE.getBehaviour(FilteringBehaviour.TYPE);

			if (inserting == null)
				return blocking;
			if (filtering != null && !filtering.test(currentItem.stack))
				return blocking;

			ItemStack before = currentItem.stack.copy();
			ItemStack remainder = inserting.insert(before);
			if (before.equals(remainder, false))
				return blocking;

			funnelTE.flap(true);
			currentItem.stack = remainder;
			beltInventory.belt.sendData();
			return true;
		}

		return false;
	}

}
