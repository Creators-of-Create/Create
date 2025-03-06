package com.simibubi.create.api.packager.unpacking;

import java.util.List;

import com.simibubi.create.content.logistics.stockTicker.PackageOrder;
import com.simibubi.create.content.logistics.stockTicker.PackageOrderCraftingContext;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * An {@link UnpackingHandler} that voids inserted items.
 */
public enum VoidingUnpackingHandler implements UnpackingHandler {
	INSTANCE;

	@Override
	public boolean unpack(Level level, BlockPos pos, BlockState state, Direction side, List<ItemStack> items, @Nullable PackageOrder orderContext, @Nullable PackageOrderCraftingContext orderCraftingContext, boolean simulate) {
		return true;
	}
}
