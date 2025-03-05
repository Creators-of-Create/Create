package com.simibubi.create.impl.unpacking;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.api.unpacking.UnpackingHandler;
import com.simibubi.create.content.logistics.stockTicker.PackageOrder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

public enum DefaultUnpackingHandler implements UnpackingHandler {
	INSTANCE;

	@Override
	public boolean unpack(Level level, BlockPos pos, BlockState state, Direction side, List<ItemStack> items, @Nullable PackageOrder order, boolean simulate) {
		BlockEntity targetBE = level.getBlockEntity(pos);
		if (targetBE == null)
			return false;

		IItemHandler targetInv = targetBE.getCapability(ForgeCapabilities.ITEM_HANDLER, side).resolve().orElse(null);
		if (targetInv == null)
			return false;

		for (int slot = 0; slot < targetInv.getSlots(); slot++) {
			ItemStack itemInSlot = targetInv.getStackInSlot(slot);
			if (!simulate)
				itemInSlot = itemInSlot.copy();

			int itemsAddedToSlot = 0;

			for (int boxSlot = 0; boxSlot < items.size(); boxSlot++) {
				ItemStack toInsert = items.get(boxSlot);
				if (toInsert.isEmpty())
					continue;

				if (targetInv.insertItem(slot, toInsert, true)
					.getCount() == toInsert.getCount())
					continue;

				if (itemInSlot.isEmpty()) {
					int maxStackSize = targetInv.getSlotLimit(slot);
					if (maxStackSize < toInsert.getCount()) {
						toInsert.shrink(maxStackSize);
						toInsert = ItemHandlerHelper.copyStackWithSize(toInsert, maxStackSize);
					} else
						items.set(boxSlot, ItemStack.EMPTY);

					itemInSlot = toInsert;
					if (!simulate)
						itemInSlot = itemInSlot.copy();

					targetInv.insertItem(slot, toInsert, simulate);
					continue;
				}

				if (!ItemHandlerHelper.canItemStacksStack(toInsert, itemInSlot))
					continue;

				int insertedAmount = toInsert.getCount() - targetInv.insertItem(slot, toInsert, simulate)
					.getCount();
				int slotLimit = (int) ((targetInv.getStackInSlot(slot)
					.isEmpty() ? itemInSlot.getMaxStackSize() / 64f : 1) * targetInv.getSlotLimit(slot));
				int insertableAmountWithPreviousItems =
					Math.min(toInsert.getCount(), slotLimit - itemInSlot.getCount() - itemsAddedToSlot);

				int added = Math.min(insertedAmount, Math.max(0, insertableAmountWithPreviousItems));
				itemsAddedToSlot += added;

				items.set(boxSlot,
					ItemHandlerHelper.copyStackWithSize(toInsert, toInsert.getCount() - added));
			}
		}

		for (ItemStack stack : items) {
			if (!stack.isEmpty()) {
				// something failed to be inserted
				return false;
			}
		}

		return true;
	}
}
