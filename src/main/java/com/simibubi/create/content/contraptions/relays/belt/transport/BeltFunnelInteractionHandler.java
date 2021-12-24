package com.simibubi.create.content.contraptions.relays.belt.transport;

import com.simibubi.create.content.contraptions.relays.belt.BeltHelper;
import com.simibubi.create.content.logistics.block.funnel.BeltFunnelBlock;
import com.simibubi.create.content.logistics.block.funnel.BeltFunnelBlock.Shape;
import com.simibubi.create.content.logistics.block.funnel.FunnelTileEntity;
import com.simibubi.create.foundation.tileEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.inventory.InvManipulationBehaviour;
import com.simibubi.create.lib.transfer.item.ItemHandlerHelper;
import com.simibubi.create.lib.util.ItemStackUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BeltFunnelInteractionHandler {

	public static boolean checkForFunnels(BeltInventory beltInventory, TransportedItemStack currentItem,
		float nextOffset) {
		boolean beltMovementPositive = beltInventory.beltMovementPositive;
		int firstUpcomingSegment = (int) Math.floor(currentItem.beltPosition);
		int step = beltMovementPositive ? 1 : -1;
		firstUpcomingSegment = Mth.clamp(firstUpcomingSegment, 0, beltInventory.belt.beltLength - 1);

		for (int segment = firstUpcomingSegment; beltMovementPositive ? segment <= nextOffset
			: segment + 1 >= nextOffset; segment += step) {
			BlockPos funnelPos = BeltHelper.getPositionForOffset(beltInventory.belt, segment)
				.above();
			Level world = beltInventory.belt.getLevel();
			BlockState funnelState = world.getBlockState(funnelPos);
			if (!(funnelState.getBlock() instanceof BeltFunnelBlock))
				continue;
			Direction funnelFacing = funnelState.getValue(BeltFunnelBlock.HORIZONTAL_FACING);
			Direction movementFacing = beltInventory.belt.getMovementFacing();
			boolean blocking = funnelFacing == movementFacing.getOpposite();
			if (funnelFacing == movementFacing)
				continue;
			if (funnelState.getValue(BeltFunnelBlock.SHAPE) == Shape.PUSHING)
				continue;

			float funnelEntry = segment + .5f;
			if (funnelState.getValue(BeltFunnelBlock.SHAPE) == Shape.EXTENDED)
				funnelEntry += .499f * (beltMovementPositive ? -1 : 1);

			boolean hasCrossed = nextOffset > funnelEntry && beltMovementPositive
				|| nextOffset < funnelEntry && !beltMovementPositive;
			if (!hasCrossed)
				return false;
			if (blocking)
				currentItem.beltPosition = funnelEntry;

			if (world.isClientSide || funnelState.getOptionalValue(BeltFunnelBlock.POWERED).orElse(false))
				if (blocking)
					return true;
				else
					continue;

			BlockEntity te = world.getBlockEntity(funnelPos);
			if (!(te instanceof FunnelTileEntity))
				return true;

			FunnelTileEntity funnelTE = (FunnelTileEntity) te;
			InvManipulationBehaviour inserting = funnelTE.getBehaviour(InvManipulationBehaviour.TYPE);
			FilteringBehaviour filtering = funnelTE.getBehaviour(FilteringBehaviour.TYPE);

			if (inserting == null || filtering != null && !filtering.test(currentItem.stack))
				if (blocking)
					return true;
				else
					continue;

			int amountToExtract = funnelTE.getAmountToExtract();
			ItemStack toInsert = currentItem.stack.copy();
			if (amountToExtract > toInsert.getCount())
				if (blocking)
					return true;
				else
					continue;

			if (amountToExtract != -1)
				toInsert.setCount(amountToExtract);

			ItemStack remainder = inserting.insert(toInsert);
			if (ItemStackUtil.equals(remainder, toInsert, false))
				if (blocking)
					return true;
				else
					continue;

			int notFilled = currentItem.stack.getCount() - toInsert.getCount();
			if (!remainder.isEmpty()) {
				remainder.grow(notFilled);
			} else if (notFilled > 0)
				remainder = ItemHandlerHelper.copyStackWithSize(currentItem.stack, notFilled);

			funnelTE.flap(true);
			funnelTE.onTransfer(toInsert);
			currentItem.stack = remainder;
			beltInventory.belt.sendData();
			if (blocking)
				return true;
		}

		return false;
	}

}
