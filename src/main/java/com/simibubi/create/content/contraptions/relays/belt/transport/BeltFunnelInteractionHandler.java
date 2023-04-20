package com.simibubi.create.content.contraptions.relays.belt.transport;

import com.simibubi.create.content.contraptions.relays.belt.BeltHelper;
import com.simibubi.create.content.logistics.block.funnel.BeltFunnelBlock;
import com.simibubi.create.content.logistics.block.funnel.BeltFunnelBlock.Shape;
import com.simibubi.create.content.logistics.block.funnel.FunnelBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.inventory.InvManipulationBehaviour;
import com.simibubi.create.foundation.item.ItemHelper.ExtractionCountMode;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemHandlerHelper;

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

			BlockEntity be = world.getBlockEntity(funnelPos);
			if (!(be instanceof FunnelBlockEntity))
				return true;

			FunnelBlockEntity funnelBE = (FunnelBlockEntity) be;
			InvManipulationBehaviour inserting = funnelBE.getBehaviour(InvManipulationBehaviour.TYPE);
			FilteringBehaviour filtering = funnelBE.getBehaviour(FilteringBehaviour.TYPE);

			if (inserting == null || filtering != null && !filtering.test(currentItem.stack))
				if (blocking)
					return true;
				else
					continue;

			int amountToExtract = funnelBE.getAmountToExtract();
			ExtractionCountMode modeToExtract = funnelBE.getModeToExtract();
			
			ItemStack toInsert = currentItem.stack.copy();
			if (amountToExtract > toInsert.getCount() && modeToExtract != ExtractionCountMode.UPTO)
				if (blocking)
					return true;
				else
					continue;

			if (amountToExtract != -1 && modeToExtract != ExtractionCountMode.UPTO) {
				toInsert.setCount(Math.min(amountToExtract, toInsert.getCount()));
				ItemStack remainder = inserting.simulate()
					.insert(toInsert);
				if (!remainder.isEmpty())
					if (blocking)
						return true;
					else
						continue;
			}

			ItemStack remainder = inserting.insert(toInsert);
			if (toInsert.equals(remainder, false))
				if (blocking)
					return true;
				else
					continue;

			int notFilled = currentItem.stack.getCount() - toInsert.getCount();
			if (!remainder.isEmpty()) {
				remainder.grow(notFilled);
			} else if (notFilled > 0)
				remainder = ItemHandlerHelper.copyStackWithSize(currentItem.stack, notFilled);

			funnelBE.flap(true);
			funnelBE.onTransfer(toInsert);
			currentItem.stack = remainder;
			beltInventory.belt.sendData();
			if (blocking)
				return true;
		}

		return false;
	}

}
