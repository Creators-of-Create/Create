package com.simibubi.create.impl.unpacking;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

import com.simibubi.create.Create;

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

	public static List<ItemStack> mergeSameItems(List<ItemStack> items) {
		final int itemsSize = items.size();
		var newList = new ArrayList<ItemStack>();
		for (int i = 0; i != itemsSize; ++i) {
			final int newListSize = newList.size();

			// merge into existing stack
			var originalStack = items.get(i);
			boolean found = false;
			for (int j = 0; j != newListSize; ++j) {
				var newItem = newList.get(j);
				if (ItemStack.isSameItemSameComponents(newItem, originalStack)) {
					newItem.setCount(newItem.getCount() + originalStack.getCount());
					found = true;
					break;
				}
			}
			// else there is no existing stack, create a new one
			if (!found) {
				newList.add(originalStack.copy());
			}
		}
		return newList;
	}

	public static int[] resortSlots(ItemStack existing, IItemHandler inv) {
		final int invSize = inv.getSlots();
		final var indexArray = new int[invSize];
		final var slotMap = new BitSet(invSize);
		var rSize = 0;
		for (int i = 0; i != invSize; ++i) {
			// put existing slots first
			if (ItemStack.isSameItemSameComponents(existing, inv.getStackInSlot(i))) {
				indexArray[rSize++] = i;
				slotMap.set(i);
			}
		}
		for (int i = 0; i != invSize; ++i) {
			if (slotMap.get(i)) continue;
			// put remaining slots later
			indexArray[rSize++] = i;
		}
		return indexArray;
	}

	@Override
	public boolean unpack(Level level, BlockPos pos, BlockState state, Direction side, List<ItemStack> items, @Nullable PackageOrderWithCrafts orderContext, boolean simulate) {
		BlockEntity targetBE = level.getBlockEntity(pos);
		if (targetBE == null)
			return false;

		IItemHandler targetInv = level.getCapability(ItemHandler.BLOCK, pos, state, targetBE, null);
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

		// simulate == true
		var mergedList = mergeSameItems(items);
		final int mergedSize = mergedList.size();
		final int targetSlots = targetInv.getSlots();
		final var slotMap = new BitSet(targetSlots);
		for (int mi = 0; mi != mergedSize; ++mi) {
			var toInsert = mergedList.get(mi);
			final var slotIndexArray = resortSlots(toInsert, targetInv);
			for (int si = 0; si != targetSlots; ++si) {
				final int slot = slotIndexArray[si];
				if (slotMap.get(slot)) continue; // ignore inserted slots
				final var result = targetInv.insertItem(slot, toInsert, true);
				if (result.getCount() == toInsert.getCount()) continue; // insert failed
				// inserted successfully, mark this slot as inserted
				// since mergedList contains no duplicated items, no other items in the list can be inserted anymore
				slotMap.set(slot);
				// save remaining for next insertion
				mergedList.set(mi, result);
				toInsert = result;
				if (result.isEmpty()) {
					break; // all inserted
				}
			}
		}

		for (ItemStack stack : mergedList) {
			if (!stack.isEmpty()) {
				// something failed to be inserted
				return false;
			}
		}

		return true;
	}
}
