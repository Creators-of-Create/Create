package com.simibubi.create.content.decoration.copycat;

import java.util.function.Predicate;

import net.minecraft.core.BlockPos;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.model.data.ModelData;

public class FilteredBlockAndTintGetter implements BlockAndTintGetter {

	private BlockAndTintGetter wrapped;
	private Predicate<BlockPos> filter;

	public FilteredBlockAndTintGetter(BlockAndTintGetter wrapped, Predicate<BlockPos> filter) {
		this.wrapped = wrapped;
		this.filter = filter;
	}

	@Override
	public BlockEntity getBlockEntity(BlockPos pPos) {
		return filter.test(pPos) ? wrapped.getBlockEntity(pPos) : null;
	}

	@Override
	public BlockState getBlockState(BlockPos pPos) {
		return filter.test(pPos) ? wrapped.getBlockState(pPos) : Blocks.AIR.defaultBlockState();
	}

	@Override
	public FluidState getFluidState(BlockPos pPos) {
		return filter.test(pPos) ? wrapped.getFluidState(pPos) : Fluids.EMPTY.defaultFluidState();
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
		return wrapped.getLightEngine();
	}

	@Override
	public CardinalLighting cardinalLighting() {
		return wrapped.cardinalLighting();
	}

	@Override
	public int getBlockTint(BlockPos pBlockPos, ColorResolver pColorResolver) {
		return wrapped.getBlockTint(pBlockPos, pColorResolver);
	}
	
	@Override
	public ModelData getModelData(BlockPos pPos) {
		return filter.test(pPos) ? wrapped.getModelData(pPos) : ModelData.EMPTY;
	}

}
