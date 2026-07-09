package com.simibubi.create.foundation.utility.worldWrappers;

import net.minecraft.core.BlockPos;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.BlockAndLightGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.model.data.ModelData;

public class WrappedBlockAndTintGetter implements BlockAndTintGetter {
	protected final BlockGetter wrapped;

	public WrappedBlockAndTintGetter(BlockGetter wrapped) {
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
	public int getMinY() {
		return wrapped.getMinY();
	}

	@Override
	public LevelLightEngine getLightEngine() {
		if (wrapped instanceof BlockAndLightGetter lightGetter)
			return lightGetter.getLightEngine();
		if (wrapped instanceof Level level)
			return level.getLightEngine();
		return BlockAndTintGetter.EMPTY.getLightEngine();
	}

	@Override
	public CardinalLighting cardinalLighting() {
		return wrapped instanceof BlockAndTintGetter tintGetter ? tintGetter.cardinalLighting() : CardinalLighting.DEFAULT;
	}

	@Override
	public int getBlockTint(BlockPos pBlockPos, ColorResolver pColorResolver) {
		return wrapped instanceof BlockAndTintGetter tintGetter ? tintGetter.getBlockTint(pBlockPos, pColorResolver) : 0xFFFFFF;
	}
	
	@Override
	public ModelData getModelData(BlockPos pPos) {
		return wrapped.getModelData(pPos);
	}
	
}
