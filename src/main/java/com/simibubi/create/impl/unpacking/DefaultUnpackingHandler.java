package com.simibubi.create.impl.unpacking;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.api.packager.unpacking.UnpackingHandler;
import com.simibubi.create.content.logistics.stockTicker.PackageOrderWithCrafts;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import net.neoforged.neoforge.capabilities.Capabilities.ItemHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

public enum DefaultUnpackingHandler implements UnpackingHandler {
	INSTANCE;

	@Override
	public boolean unpack(Level level, BlockPos pos, BlockState state, Direction side, List<ItemStack> items, @Nullable PackageOrderWithCrafts orderContext, boolean simulate) {
		BlockEntity targetBE = level.getBlockEntity(pos);
		if (targetBE == null)
			return false;

		IItemHandler targetInv = level.getCapability(ItemHandler.BLOCK, pos, state, targetBE, side);
		if (targetInv == null)
			return false;

		if (!simulate) {
			/*
			 * Some mods do not support slot-by-slot precision during simulate = false.
			 * Faulty interactions may lead to voiding of items, but the simulate pass should
			 * already have correctly identified there to be enough space for everything.
			 */
			for (ItemStack itemStack : items)
				ItemHandlerHelper.insertItemStacked(targetInv, itemStack.copy(), false);
			return true;
		}

		for (int slot = 0; slot < targetInv.getSlots(); slot++) {
			ItemStack itemInSlot = targetInv.getStackInSlot(slot);
			int itemsAddedToSlot = 0;

			for (int boxSlot = 0; boxSlot < items.size(); boxSlot++) {
				ItemStack toInsert = items.get(boxSlot);

				if (toInsert.isEmpty())
					continue;

				if (!itemInSlot.isEmpty() && !ItemStack.isSameItemSameComponents(toInsert, itemInSlot))
					continue;

				int simulatedAmount = itemsAddedToSlot + toInsert.getCount();
				ItemStack simulatedStack = toInsert.copyWithCount(simulatedAmount);
				int totalInsertable = simulatedAmount - targetInv.insertItem(slot, simulatedStack, true).getCount();

				int added = Math.min(toInsert.getCount(),
					Math.max(0, totalInsertable - itemsAddedToSlot)
				);

				if (added == 0)
					continue;

				if (itemInSlot.isEmpty())
					itemInSlot = toInsert.copy();

				itemsAddedToSlot += added;

				int remaining = toInsert.getCount() - added;

				items.set(boxSlot, remaining == 0 ? ItemStack.EMPTY : toInsert.copyWithCount(remaining));
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
