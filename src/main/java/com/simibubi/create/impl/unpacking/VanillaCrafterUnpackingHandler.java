package com.simibubi.create.impl.unpacking;

import com.simibubi.create.api.packager.unpacking.UnpackingHandler;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.stockTicker.PackageOrderWithCrafts;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CrafterBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public enum VanillaCrafterUnpackingHandler implements UnpackingHandler {
	INSTANCE;


	@Override
	public boolean unpack(Level level, BlockPos pos, BlockState state, Direction side, List<ItemStack> items, @Nullable PackageOrderWithCrafts orderContext, boolean simulate) {
		if (!PackageOrderWithCrafts.hasCraftingInformation(orderContext))
			return DEFAULT.unpack(level, pos, state, side, items, orderContext, simulate);

		List<BigItemStack> craftingContext = orderContext.getCraftingInformation();

		BlockEntity be = level.getBlockEntity(pos);
		if(!(be instanceof CrafterBlockEntity crafter))
			return false;
		int max = Math.min(9, craftingContext.size());
		outer: for (int i = 0; i < max; i++) {
			BigItemStack targetStack = craftingContext.get(i);
			if (targetStack.stack.isEmpty())
				continue;

			if (crafter.isSlotDisabled(i))
				continue;

			if (!crafter.getItem(i).isEmpty())
				continue;

			for (ItemStack stack : items) {
				if (ItemStack.isSameItemSameComponents(stack, targetStack.stack)) {
					ItemStack toInsert = stack.copyWithCount(1);
					if (crafter.canPlaceItem(i, toInsert)) {
						if (!simulate) {
							crafter.setItem(i, toInsert);
						}
						stack.shrink(1);
						continue outer;
					}
				}
			}
		}

		for (ItemStack item : items) {
			if (!item.isEmpty())
				return false;
		}

		return true;
	}
}
