package com.simibubi.create.foundation.utility.worldWrappers;

import java.util.function.BiFunction;

import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;

public class RayTraceWorld implements IBlockReader {

	private IWorld template;
	private BiFunction<BlockPos, BlockState, BlockState> stateGetter;

	public RayTraceWorld(IWorld template, BiFunction<BlockPos, BlockState, BlockState> stateGetter) {
		this.template = template;
		this.stateGetter = stateGetter;
	}

	@Override
	public TileEntity getTileEntity(BlockPos pos) {
		return template.getTileEntity(pos);
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
