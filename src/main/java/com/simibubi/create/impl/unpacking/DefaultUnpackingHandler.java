package com.simibubi.create.impl.unpacking;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.AllEntityTypes;

import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.box.PackageStyles;

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

		List<ItemStack> remainderList = new ArrayList<>();

		for (ItemStack itemStack : items) {
			var remainder = ItemHandlerHelper.insertItemStacked(targetInv, itemStack.copy(), simulate);
			if (!remainder.isEmpty()) {
				if (simulate) {
					return false;
				} else {
					remainderList.add(remainder);
				}
			}
		}

		if (!simulate && !remainderList.isEmpty()) {
			var itemPos = Vec3.atCenterOf(pos);
			for (var itemStack : remainderList) {
				var itemEntity = new ItemEntity(level, itemPos.x, itemPos.y, itemPos.z, itemStack);
				level.addFreshEntity(itemEntity);
			}
		}

		return true;
	}
}
