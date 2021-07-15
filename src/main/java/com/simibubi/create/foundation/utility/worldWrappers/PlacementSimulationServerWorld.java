package com.simibubi.create.foundation.utility.worldWrappers;

import java.util.HashMap;
import java.util.function.Predicate;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

public class PlacementSimulationServerWorld extends WrappedServerWorld {
	public HashMap<BlockPos, BlockState> blocksAdded;

	public PlacementSimulationServerWorld(ServerWorld wrapped) {
		super(wrapped);
		blocksAdded = new HashMap<>();
	}
	
	public void clear() {
		blocksAdded.clear();
	}

	@Override
	public boolean setBlock(BlockPos pos, BlockState newState, int flags) {
		blocksAdded.put(pos.immutable(), newState);
		return true;
	}

	@Override
	public boolean setBlockAndUpdate(BlockPos pos, BlockState state) {
		return setBlock(pos, state, 0);
	}

	@Override
	public boolean isStateAtPosition(BlockPos pos, Predicate<BlockState> condition) {
		return condition.test(getBlockState(pos));
	}
	
	@Override
	public boolean isLoaded(BlockPos pos) {
		return true;
	}
	
	@Override
	public boolean isAreaLoaded(BlockPos center, int range) {
		return true;
	}

	@Override
	public BlockState getBlockState(BlockPos pos) {
		if (blocksAdded.containsKey(pos))
			return blocksAdded.get(pos);
		return Blocks.AIR.defaultBlockState();
	}
	
}
