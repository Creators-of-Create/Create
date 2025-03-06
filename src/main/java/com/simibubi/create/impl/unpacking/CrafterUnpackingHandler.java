package com.simibubi.create.impl.unpacking;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.api.unpacking.UnpackingHandler;
import com.simibubi.create.content.kinetics.crafter.ConnectedInputHandler.ConnectedInput;
import com.simibubi.create.content.kinetics.crafter.MechanicalCrafterBlockEntity;
import com.simibubi.create.content.kinetics.crafter.MechanicalCrafterBlockEntity.Inventory;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.stockTicker.PackageOrder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import net.minecraftforge.items.ItemHandlerHelper;

public enum CrafterUnpackingHandler implements UnpackingHandler {
	INSTANCE;

	@Override
	public boolean unpack(Level level, BlockPos pos, BlockState state, Direction side, List<ItemStack> items, @Nullable PackageOrder order, boolean simulate) {
		if (order == null) {
			return DEFAULT.unpack(level, pos, state, side, items, null, simulate);
		}

		BlockEntity be = level.getBlockEntity(pos);
		if (!(be instanceof MechanicalCrafterBlockEntity crafter))
			return false;

		ConnectedInput input = crafter.getInput();
		List<Inventory> inventories = input.getInventories(level, pos);
		if (inventories.isEmpty())
			return false;

		// insert in the order's defined ordering
		int max = Math.min(inventories.size(), order.stacks().size());
		for (int i = 0; i < max; i++) {
			BigItemStack targetStack = order.stacks().get(i);
			if (targetStack.stack.isEmpty())
				continue;

			Inventory inventory = inventories.get(i);
			// if there's already an item here, no point in trying
			if (!inventory.getStackInSlot(0).isEmpty())
				continue;

			// go through each item in the box and try insert if it matches the target
			for (ItemStack stack : items) {
				if (ItemHandlerHelper.canItemStacksStack(stack, targetStack.stack)) {
					ItemStack toInsert = stack.copyWithCount(1);
					if (inventory.insertItem(0, toInsert, simulate).isEmpty()) {
						stack.shrink(1);
					}
				}
			}
		}

		// if anything is still non-empty insertion failed
		for (ItemStack item : items) {
			if (!item.isEmpty()) {
				return false;
			}
		}

		if (!simulate) {
			crafter.checkCompletedRecipe(true);
		}

		return true;
	}
}
