package com.simibubi.create.impl.unpacking;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.Vec3;

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
			List<ItemStack> remainderList = new ArrayList<>();

			for (ItemStack stack : items) {
				ItemStack remainder = ItemHandlerHelper.insertItemStacked(targetInv, stack.copy(), false);
				if (!remainder.isEmpty()) {
					remainderList.add(remainder);
				}
			}

			/*
			 * Some mods may have inconsistency between simulate pass and actually pushing items
			 * and possibly yield some leftover items
			 */
			if (!remainderList.isEmpty()) {
				var itemPos = Vec3.atCenterOf(pos);
				for (var itemStack : remainderList) {
					var itemEntity = new ItemEntity(level, itemPos.x, itemPos.y, itemPos.z, itemStack);
					level.addFreshEntity(itemEntity);
				}
			}

			return true;
		}

		for (int boxSlot = 0; boxSlot < items.size(); boxSlot++) {
			ItemStack boxItemStack = items.get(boxSlot);
			if (boxItemStack.isEmpty())
				continue;

			int amountToInsert = 0;
			// iterate over contents of the box and count similar items
			for (int otherBoxSlot = boxSlot; otherBoxSlot < items.size(); otherBoxSlot++) {
				ItemStack otherBoxItemStack = items.get(otherBoxSlot);
				if (!ItemStack.isSameItemSameComponents(boxItemStack, otherBoxItemStack))
					continue;

				int stackSize = otherBoxItemStack.getCount();
				amountToInsert += stackSize;
				items.set(otherBoxSlot, otherBoxItemStack.copyWithCount(0));
			}

			// compress similar items into one stack so that it's easier to work with
			ItemStack itemToInsert = boxItemStack.copyWithCount(amountToInsert);
			for (int invSlot = 0; invSlot < targetInv.getSlots(); invSlot++) {
				itemToInsert = targetInv.insertItem(invSlot, itemToInsert.copy(), true);
			}

			if (!itemToInsert.isEmpty()) {
				return false;
			}
		}

		return true;
	}
}
