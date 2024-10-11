package com.simibubi.create.foundation.utility.worldWrappers;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;

public class WrappedBlockAndTintGetter implements BlockAndTintGetter {
	protected final BlockAndTintGetter wrapped;

	public WrappedBlockAndTintGetter(BlockAndTintGetter wrapped) {
		this.wrapped = wrapped;
	}

	@Override
	public BlockEntity getBlockEntity(BlockPos pos) {
		return wrapped.getBlockEntity(pos);
	}

	@Override
	public BlockState getBlockState(BlockPos pos) {
		return wrapped.getBlockState(pos);
	}

	@Override
	public FluidState getFluidState(BlockPos pos) {
		return wrapped.getFluidState(pos);
	}

	@Override
	public int getHeight() {
		return wrapped.getHeight();
	}

	@Override
	public int getMinBuildHeight() {
		return wrapped.getMinBuildHeight();
	}

	@Override
	public float getShade(Direction pDirection, boolean pShade) {
		return wrapped.getShade(pDirection, pShade);
	}

	@Override
	public LevelLightEngine getLightEngine() {
		return wrapped.getLightEngine();
	}

	@Override
	public int getBlockTint(BlockPos pBlockPos, ColorResolver pColorResolver) {
		return wrapped.getBlockTint(pBlockPos, pColorResolver);
	}
}
