package com.simibubi.create.foundation.utility.worldWrappers;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple implementation of BlockGetter for testing occlusion culling. (For example, to test copycats)
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class OcclusionTestLevel implements BlockGetter {
	private final Map<BlockPos, BlockState> blocks = new HashMap<>();

	public void setBlock(BlockPos pos, BlockState state) {
		blocks.put(pos.immutable(), state);
	}

	public void clear() {
		blocks.clear();
	}

	@Nullable
	@Override
	public BlockEntity getBlockEntity(BlockPos pos) {
		return null;
	}

	@Override
	public BlockState getBlockState(BlockPos pos) {
		return blocks.get(pos);
	}

	@Override
	public FluidState getFluidState(BlockPos pos) {
		return Fluids.EMPTY.defaultFluidState();
	}

	@Override
	public int getHeight() {
		return 256;
	}

	@Override
	public int getMinBuildHeight() {
		return 0;
	}
}
