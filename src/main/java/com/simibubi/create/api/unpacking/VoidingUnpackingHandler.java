package com.simibubi.create.api.unpacking;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.content.logistics.stockTicker.PackageOrder;

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
	public boolean unpack(Level level, BlockPos pos, BlockState state, Direction side, List<ItemStack> items, @Nullable PackageOrder order, boolean simulate) {
		return true;
	}
}
