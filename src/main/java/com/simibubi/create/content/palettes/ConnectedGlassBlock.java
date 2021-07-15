package com.simibubi.create.content.palettes;

import net.minecraft.block.BlockState;
import net.minecraft.block.GlassBlock;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import net.minecraft.block.AbstractBlock.Properties;

public class ConnectedGlassBlock extends GlassBlock {

	public ConnectedGlassBlock(Properties p_i48392_1_) {
		super(p_i48392_1_);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean skipRendering(BlockState state, BlockState adjacentBlockState, Direction side) {
		return adjacentBlockState.getBlock() instanceof ConnectedGlassBlock ? true
			: super.skipRendering(state, adjacentBlockState, side);
	}

	@Override
	public boolean shouldDisplayFluidOverlay(BlockState state, IBlockDisplayReader world, BlockPos pos, FluidState fluidState) {
		return true;
	}
}
