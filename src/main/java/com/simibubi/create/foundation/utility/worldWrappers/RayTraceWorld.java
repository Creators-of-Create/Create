package com.simibubi.create.foundation.utility.worldWrappers;

import java.util.function.BiFunction;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;

public class RayTraceWorld implements BlockGetter {

	private LevelAccessor template;
	private BiFunction<BlockPos, BlockState, BlockState> stateGetter;

	public RayTraceWorld(LevelAccessor template, BiFunction<BlockPos, BlockState, BlockState> stateGetter) {
		this.template = template;
		this.stateGetter = stateGetter;
	}

	@Override
	public BlockEntity getBlockEntity(BlockPos pos) {
		return template.getBlockEntity(pos);
	}

	@Override
	public BlockState getBlockState(BlockPos pos) {
		return stateGetter.apply(pos, template.getBlockState(pos));
	}

	@Override
	public FluidState getFluidState(BlockPos pos) {
		return template.getFluidState(pos);
	}

}
